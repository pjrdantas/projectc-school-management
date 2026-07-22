package br.com.escola.enrollmentdocumentservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.enrollmentdocumentservice.infra.config.DocumentoArquivoStorageProperties;
import br.com.escola.enrollmentdocumentservice.infra.storage.LocalDocumentoArquivoStorageAdapter;

class DocumentoBackfillExecutorTest {

    @Test
    void deveMigrarDocumentoDeAlunoReconciliarEManterRegistroLocal() throws Exception {
        JdbcTemplate source = sourceDatabase();
        JdbcTemplate target = targetDatabase();
        Path sourceRoot = Files.createTempDirectory("document-backfill-source-");
        Path targetRoot = Files.createTempDirectory("document-backfill-target-");
        UUID documentoId = UUID.fromString("00000000-0000-0000-0000-000000000911");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000912");
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 7, 22, 11, 30);
        Files.createDirectories(sourceRoot.resolve("documentos"));
        Files.writeString(sourceRoot.resolve("documentos/rg.pdf"), "conteudo legado");
        prepararOrigem(source, documentoId, escolaId, alunoId, uploadedAt);

        LocalDocumentoArquivoStorageAdapter storage = new LocalDocumentoArquivoStorageAdapter(
                new DocumentoArquivoStorageProperties(targetRoot.toString()));
        DocumentoBackfillExecutor executor = new DocumentoBackfillExecutor(source, target, storage, sourceRoot, 1);

        DocumentoBackfillReport first = executor.execute();
        target.update("""
                INSERT INTO student_document (
                    id, school_id, student_id, document_type, uploaded_at, storage_reference
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), escolaId, UUID.randomUUID(), "CPF", uploadedAt, UUID.randomUUID().toString());
        DocumentoBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(target.queryForObject("SELECT COUNT(1) FROM student_document", Integer.class)).isEqualTo(2);
        assertThat(target.queryForObject("SELECT document_type FROM student_document WHERE id = ?", String.class, documentoId))
                .isEqualTo("RG");
        String storageReference = target.queryForObject(
                "SELECT storage_reference FROM student_document WHERE id = ?", String.class, documentoId);
        try (var input = storage.abrirConteudo(storageReference)) {
            assertThat(new String(input.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("conteudo legado");
        }
    }

    private JdbcTemplate sourceDatabase() {
        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:document-backfill-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
        source.execute("DROP ALL OBJECTS");
        source.execute("CREATE TABLE documento (id_documento UUID PRIMARY KEY, id_tipo_documento UUID, nome_arquivo VARCHAR(255), url_arquivo VARCHAR(500), data_upload TIMESTAMP, observacao VARCHAR(500), ativo BOOLEAN)");
        source.execute("CREATE TABLE tipo_documento (id_tipo_documento UUID PRIMARY KEY, codigo VARCHAR(80))");
        source.execute("CREATE TABLE pessoa_documento (id_pessoa_documento UUID PRIMARY KEY, id_pessoa UUID, id_documento UUID)");
        source.execute("CREATE TABLE aluno (id_aluno UUID PRIMARY KEY, id_pessoa UUID)");
        source.execute("CREATE TABLE pessoa (id_pessoa UUID PRIMARY KEY, id_escola UUID)");
        return source;
    }

    private JdbcTemplate targetDatabase() {
        String url = "jdbc:h2:mem:document-backfill-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate();
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }

    private void prepararOrigem(
            JdbcTemplate source,
            UUID documentoId,
            UUID escolaId,
            UUID alunoId,
            LocalDateTime uploadedAt) {
        UUID pessoaId = UUID.randomUUID();
        UUID tipoDocumentoId = UUID.randomUUID();
        source.update("INSERT INTO pessoa VALUES (?, ?)", pessoaId, escolaId);
        source.update("INSERT INTO aluno VALUES (?, ?)", alunoId, pessoaId);
        source.update("INSERT INTO tipo_documento VALUES (?, ?)", tipoDocumentoId, "RG");
        source.update("INSERT INTO documento VALUES (?, ?, ?, ?, ?, ?, ?)",
                documentoId, tipoDocumentoId, "rg.pdf", "/documentos/rg.pdf", uploadedAt, "Migrado", true);
        source.update("INSERT INTO pessoa_documento VALUES (?, ?, ?)", UUID.randomUUID(), pessoaId, documentoId);
    }
}
