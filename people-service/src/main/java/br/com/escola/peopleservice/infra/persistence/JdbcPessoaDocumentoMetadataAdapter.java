package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

@Component
public class JdbcPessoaDocumentoMetadataAdapter implements PessoaDocumentoMetadataPort {

    private static final String BASE_SELECT = """
            SELECT id_pessoa_documento,
                   id_pessoa,
                   id_documento,
                   id_tipo_documento,
                   tipo_documento_codigo,
                   tipo_documento_descricao,
                   numero_documento,
                   caminho_arquivo,
                   observacao,
                   data_upload,
                   created_at
            FROM people_documento_read_model
            WHERE id_escola = ?
            """;

    private final PeopleReadModelMigrationProperties properties;

    public JdbcPessoaDocumentoMetadataAdapter(PeopleReadModelMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
        List<PessoaDocumentoMetadataResponse> documentos = consultar(
                BASE_SELECT + " AND id_documento = ? ORDER BY data_upload DESC, created_at DESC, id_pessoa_documento",
                escolaId,
                documentoId);
        if (documentos.size() > 1) {
            throw new IllegalStateException("document-metadata-duplicate-document-id");
        }
        return documentos.stream().findFirst();
    }

    @Override
    public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
        return consultar(
                BASE_SELECT + " AND id_pessoa = ? ORDER BY data_upload DESC, created_at DESC, id_pessoa_documento",
                escolaId,
                pessoaId);
    }

    private List<PessoaDocumentoMetadataResponse> consultar(String sql, UUID escolaId, UUID filtroId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("document-metadata-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, escolaId);
            statement.setObject(2, filtroId);
            try (var resultSet = statement.executeQuery()) {
                List<PessoaDocumentoMetadataResponse> documentos = new ArrayList<>();
                while (resultSet.next()) {
                    documentos.add(new PessoaDocumentoMetadataResponse(
                            resultSet.getObject("id_pessoa_documento", UUID.class),
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_documento", UUID.class),
                            resultSet.getObject("id_tipo_documento", UUID.class),
                            resultSet.getString("tipo_documento_codigo"),
                            resultSet.getString("tipo_documento_descricao"),
                            resultSet.getString("numero_documento"),
                            resultSet.getString("caminho_arquivo"),
                            resultSet.getString("observacao"),
                            resultSet.getObject("data_upload", OffsetDateTime.class)));
                }
                return List.copyOf(documentos);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("document-metadata-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("document-metadata-local-read-driver-not-found", ex);
        }
    }
}

