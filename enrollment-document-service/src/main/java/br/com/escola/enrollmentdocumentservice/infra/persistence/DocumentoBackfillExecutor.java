package br.com.escola.enrollmentdocumentservice.infra.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;

/** Imports legacy student documents only when their local content is explicitly available. */
public class DocumentoBackfillExecutor {

    private static final String STUDENT_DOCUMENTS = "student_document";

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final DocumentoArquivoStoragePort storagePort;
    private final Path sourceStorageRoot;
    private final int batchSize;

    public DocumentoBackfillExecutor(
            JdbcTemplate source,
            JdbcTemplate target,
            DocumentoArquivoStoragePort storagePort,
            Path sourceStorageRoot,
            int batchSize) {
        this.source = source;
        this.target = target;
        this.storagePort = storagePort;
        this.sourceStorageRoot = sourceStorageRoot.toAbsolutePath().normalize();
        this.batchSize = batchSize;
    }

    public DocumentoBackfillReport execute() {
        List<DocumentoAlunoRow> documentos = page();
        documentos.forEach(this::upsert);
        int reconciled = documentos.stream().mapToInt(this::reconcile).sum();
        Map<String, Integer> rows = Map.of(STUDENT_DOCUMENTS, documentos.size());
        return new DocumentoBackfillReport(rows, rows, Map.of(STUDENT_DOCUMENTS, reconciled), reconciled == documentos.size());
    }

    private void upsert(DocumentoAlunoRow row) {
        String storageReference = target.query("SELECT storage_reference FROM student_document WHERE id = ?",
                result -> result.next() ? result.getString(1) : null, row.id());
        if (storageReference == null || storageReference.isBlank()) {
            storageReference = copiarConteudo(row);
        }
        target.update("""
                UPDATE student_document
                   SET school_id = ?, student_id = ?, document_type = ?, file_name = ?, file_url = ?,
                       document_number = ?, file_path = ?, storage_reference = ?, uploaded_at = ?,
                       updated_at = ?, note = ?
                 WHERE id = ?
                """, row.escolaId(), row.alunoId(), row.tipoDocumento(), row.nomeArquivo(), row.caminhoArquivo(),
                row.nomeArquivo(), row.caminhoArquivo(), storageReference, row.dataUpload(), row.dataUpload(),
                row.observacao(), row.id());
        Integer existing = target.queryForObject("SELECT COUNT(1) FROM student_document WHERE id = ?", Integer.class, row.id());
        if (existing != null && existing == 1) {
            return;
        }
        target.update("""
                INSERT INTO student_document (
                    id, school_id, student_id, document_type, file_name, file_url, document_number, file_path,
                    storage_reference, uploaded_at, updated_at, note
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.escolaId(), row.alunoId(), row.tipoDocumento(), row.nomeArquivo(), row.caminhoArquivo(),
                row.nomeArquivo(), row.caminhoArquivo(), storageReference, row.dataUpload(), row.dataUpload(), row.observacao());
    }

    private String copiarConteudo(DocumentoAlunoRow row) {
        try (InputStream input = abrirFonte(row.caminhoArquivo())) {
            return storagePort.armazenar(new DocumentoArquivoStoragePort.ConteudoArquivoDocumento(
                    row.nomeArquivo(), Files.probeContentType(resolverFonte(row.caminhoArquivo())),
                    Files.size(resolverFonte(row.caminhoArquivo())), input)).referenciaArmazenamento();
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel copiar o conteudo do documento legado", exception);
        }
    }

    private int reconcile(DocumentoAlunoRow row) {
        String storageReference = target.query("""
                SELECT storage_reference FROM student_document
                 WHERE id = ? AND school_id = ? AND student_id = ? AND document_type = ?
                   AND file_name = ? AND file_path = ? AND deleted_at IS NULL
                """, result -> result.next() ? result.getString(1) : null,
                row.id(), row.escolaId(), row.alunoId(), row.tipoDocumento(), row.nomeArquivo(), row.caminhoArquivo());
        if (storageReference == null || storageReference.isBlank()) {
            return 0;
        }
        try (InputStream ignored = storagePort.abrirConteudo(storageReference)) {
            return 1;
        } catch (IOException | RuntimeException exception) {
            return 0;
        }
    }

    private InputStream abrirFonte(String caminhoArquivo) throws IOException {
        return Files.newInputStream(resolverFonte(caminhoArquivo));
    }

    private Path resolverFonte(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.isBlank() || caminhoArquivo.matches("^[A-Za-z][A-Za-z0-9+.-]*://.*")) {
            throw new IllegalArgumentException("Caminho de documento legado invalido");
        }
        String relative = caminhoArquivo.replace('\\', '/').replaceFirst("^/+", "");
        Path arquivo = sourceStorageRoot.resolve(relative).normalize();
        if (!arquivo.startsWith(sourceStorageRoot)) {
            throw new IllegalArgumentException("Caminho de documento legado invalido");
        }
        return arquivo;
    }

    private List<DocumentoAlunoRow> page() {
        List<DocumentoAlunoRow> documents = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList("""
                    SELECT d.id_documento, p.id_escola, a.id_aluno, td.codigo, d.nome_arquivo,
                           d.url_arquivo, d.data_upload, d.observacao
                      FROM documento d
                      JOIN tipo_documento td ON td.id_tipo_documento = d.id_tipo_documento
                      JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
                      JOIN aluno a ON a.id_pessoa = pd.id_pessoa
                      JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                     WHERE d.ativo = TRUE
                     ORDER BY d.id_documento
                     LIMIT ? OFFSET ?
                    """, batchSize, offset);
            batch.forEach(row -> documents.add(new DocumentoAlunoRow(
                    uuid(row, "id_documento"), uuid(row, "id_escola"), uuid(row, "id_aluno"),
                    text(row, "codigo"), text(row, "nome_arquivo"), text(row, "url_arquivo"),
                    timestamp(row, "data_upload"), text(row, "observacao"))));
            offset += batch.size();
        } while (batch.size() == batchSize);
        return documents;
    }

    private UUID uuid(Map<String, Object> row, String column) { return (UUID) row.get(column); }
    private String text(Map<String, Object> row, String column) { return (String) row.get(column); }
    private LocalDateTime timestamp(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof java.sql.Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) value;
    }

    private record DocumentoAlunoRow(
            UUID id,
            UUID escolaId,
            UUID alunoId,
            String tipoDocumento,
            String nomeArquivo,
            String caminhoArquivo,
            LocalDateTime dataUpload,
            String observacao) {
        private DocumentoAlunoRow {
            Objects.requireNonNull(id);
            Objects.requireNonNull(escolaId);
            Objects.requireNonNull(alunoId);
        }
    }
}
