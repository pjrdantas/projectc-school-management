package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

class JdbcPessoaDocumentoMetadataAdapterTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final UUID OUTRA_ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID PESSOA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENTO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void deveLerDocumentoPorIdEEscola() throws Exception {
        String url = h2Url("document_metadata_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPessoaDocumentoMetadataAdapter adapter = adapter(url);

        var response = adapter.buscarDocumentoPorId(DOCUMENTO_ID, ESCOLA_ID);

        assertThat(response).isPresent();
        assertThat(response.get().pessoaId()).isEqualTo(PESSOA_ID);
        assertThat(response.get().tipoDocumentoCodigo()).isEqualTo("CPF");
        assertThat(response.get().numeroDocumento()).isEqualTo("12345678900");
        assertThat(response.get().caminhoArquivo()).isEqualTo("/documentos/cpf-frente.pdf");
    }

    @Test
    void deveListarDocumentosPorPessoaOrdenadosPorDataUploadMaisRecente() throws Exception {
        String url = h2Url("document_metadata_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPessoaDocumentoMetadataAdapter adapter = adapter(url);

        var response = adapter.listarDocumentosPorPessoa(PESSOA_ID, ESCOLA_ID);

        assertThat(response).hasSize(2);
        assertThat(response).extracting("documentoId")
                .containsExactly(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        UUID.fromString("22222222-2222-2222-2222-222222222222"));
    }

    @Test
    void naoRetornaDocumentoDeOutraEscola() throws Exception {
        String url = h2Url("document_metadata_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPessoaDocumentoMetadataAdapter adapter = adapter(url);

        var response = adapter.buscarDocumentoPorId(DOCUMENTO_ID, OUTRA_ESCOLA_ID);
        var lista = adapter.listarDocumentosPorPessoa(PESSOA_ID, OUTRA_ESCOLA_ID);

        assertThat(response).isEmpty();
        assertThat(lista).isEmpty();
    }

    @Test
    void deveBloquearDuplicidadeDoMesmoDocumentoNaMesmaEscola() throws Exception {
        String url = h2Url("document_metadata_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, true);
        JdbcPessoaDocumentoMetadataAdapter adapter = adapter(url);

        assertThatThrownBy(() -> adapter.buscarDocumentoPorId(DOCUMENTO_ID, ESCOLA_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("document-metadata-duplicate-document-id");
    }

    @Test
    void deveFalharQuandoUrlLocalNaoFoiConfigurada() {
        JdbcPessoaDocumentoMetadataAdapter adapter = new JdbcPessoaDocumentoMetadataAdapter(
                new PeopleReadModelMigrationProperties("", "sa", "", "org.h2.Driver", List.of()));

        assertThatThrownBy(() -> adapter.listarDocumentosPorPessoa(PESSOA_ID, ESCOLA_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("document-metadata-local-read-url-required");
    }

    private JdbcPessoaDocumentoMetadataAdapter adapter(String url) {
        return new JdbcPessoaDocumentoMetadataAdapter(
                new PeopleReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaEPopular(String url, boolean duplicarDocumento) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE people_documento_read_model (
                        id_pessoa_documento UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_documento UUID NOT NULL,
                        id_tipo_documento UUID NOT NULL,
                        tipo_documento_codigo VARCHAR(50) NOT NULL,
                        tipo_documento_descricao VARCHAR(150) NOT NULL,
                        numero_documento VARCHAR(50),
                        caminho_arquivo VARCHAR(255) NOT NULL,
                        observacao VARCHAR(255),
                        data_upload TIMESTAMP WITH TIME ZONE,
                        id_escola UUID NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO people_documento_read_model (
                        id_pessoa_documento, id_pessoa, id_documento, id_tipo_documento, tipo_documento_codigo,
                        tipo_documento_descricao, numero_documento, caminho_arquivo, observacao, data_upload,
                        id_escola, created_at
                    ) VALUES
                    (
                        'aaaaaaaa-1111-1111-1111-111111111111',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '11111111-1111-1111-1111-111111111111',
                        '99999999-9999-9999-9999-999999999991',
                        'CPF',
                        'CPF',
                        '12345678900',
                        '/documentos/cpf-frente.pdf',
                        'Frente',
                        TIMESTAMP WITH TIME ZONE '2026-01-10 10:00:00+00:00',
                        '00000000-0000-0000-0000-000000000047',
                        TIMESTAMP WITH TIME ZONE '2026-01-10 10:00:00+00:00'
                    ),
                    (
                        'aaaaaaaa-2222-2222-2222-222222222222',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '22222222-2222-2222-2222-222222222222',
                        '99999999-9999-9999-9999-999999999992',
                        'RG',
                        'Registro Geral',
                        'MG123456',
                        '/documentos/rg.pdf',
                        'Verso',
                        TIMESTAMP WITH TIME ZONE '2026-01-09 10:00:00+00:00',
                        '00000000-0000-0000-0000-000000000047',
                        TIMESTAMP WITH TIME ZONE '2026-01-09 10:00:00+00:00'
                    ),
                    (
                        'bbbbbbbb-1111-1111-1111-111111111111',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '33333333-3333-3333-3333-333333333333',
                        '99999999-9999-9999-9999-999999999993',
                        'CPF',
                        'CPF',
                        '99999999999',
                        '/documentos/externo.pdf',
                        NULL,
                        TIMESTAMP WITH TIME ZONE '2026-01-08 10:00:00+00:00',
                        '00000000-0000-0000-0000-000000000099',
                        TIMESTAMP WITH TIME ZONE '2026-01-08 10:00:00+00:00'
                    )
                    """);
            if (duplicarDocumento) {
                statement.execute("""
                        INSERT INTO people_documento_read_model (
                            id_pessoa_documento, id_pessoa, id_documento, id_tipo_documento, tipo_documento_codigo,
                            tipo_documento_descricao, numero_documento, caminho_arquivo, observacao, data_upload,
                            id_escola, created_at
                        ) VALUES
                        (
                            'aaaaaaaa-3333-3333-3333-333333333333',
                            'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                            '11111111-1111-1111-1111-111111111111',
                            '99999999-9999-9999-9999-999999999991',
                            'CPF',
                            'CPF',
                            '12345678900',
                            '/documentos/cpf-duplicado.pdf',
                            'Duplicado',
                            TIMESTAMP WITH TIME ZONE '2026-01-07 10:00:00+00:00',
                            '00000000-0000-0000-0000-000000000047',
                            TIMESTAMP WITH TIME ZONE '2026-01-07 10:00:00+00:00'
                        )
                        """);
            }
        }
    }
}

