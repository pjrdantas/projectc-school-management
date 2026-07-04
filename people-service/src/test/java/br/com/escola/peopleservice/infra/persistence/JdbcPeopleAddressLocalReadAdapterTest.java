package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;

class JdbcPeopleAddressLocalReadAdapterTest {

    private static final UUID PESSOA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Test
    void deveLerEnderecoPrincipalLocalPorPessoaEEscola() throws Exception {
        String url = h2Url("address_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPeopleAddressLocalReadAdapter adapter = adapter(url);

        var response = adapter.buscarEnderecoPrincipalPorPessoa(PESSOA_ID, ESCOLA_ID);

        assertThat(response).isPresent();
        assertThat(response.get().pessoaId()).isEqualTo(PESSOA_ID);
        assertThat(response.get().tipoEnderecoCodigo()).isEqualTo("RESIDENCIAL");
        assertThat(response.get().tipoEnderecoDescricao()).isEqualTo("Residencial");
        assertThat(response.get().principal()).isTrue();
        assertThat(response.get().cep()).isEqualTo("01001000");
        assertThat(response.get().logradouro()).isEqualTo("Praca da Se");
        assertThat(response.get().cidade()).isEqualTo("Sao Paulo");
        assertThat(response.get().uf()).isEqualTo("SP");
    }

    @Test
    void deveListarEnderecosLocaisPorPessoaEEscolaComPrincipalPrimeiro() throws Exception {
        String url = h2Url("address_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPeopleAddressLocalReadAdapter adapter = adapter(url);

        var response = adapter.listarEnderecosPorPessoa(PESSOA_ID, ESCOLA_ID);

        assertThat(response).hasSize(2);
        assertThat(response).extracting("principal").containsExactly(true, false);
        assertThat(response).extracting("cep").containsExactly("01001000", "20040002");
    }

    @Test
    void naoRetornaEnderecoDePessoaDeOutraEscola() throws Exception {
        String url = h2Url("address_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, false);
        JdbcPeopleAddressLocalReadAdapter adapter = adapter(url);

        var principal = adapter.buscarEnderecoPrincipalPorPessoa(
                PESSOA_ID,
                UUID.fromString("00000000-0000-0000-0000-000000000099"));
        var lista = adapter.listarEnderecosPorPessoa(
                PESSOA_ID,
                UUID.fromString("00000000-0000-0000-0000-000000000099"));

        assertThat(principal).isEmpty();
        assertThat(lista).isEmpty();
    }

    @Test
    void deveBloquearQuandoPessoaTemMaisDeUmEnderecoPrincipal() throws Exception {
        String url = h2Url("address_local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url, true);
        JdbcPeopleAddressLocalReadAdapter adapter = adapter(url);

        assertThatThrownBy(() -> adapter.buscarEnderecoPrincipalPorPessoa(PESSOA_ID, ESCOLA_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("address-principal-rule-violated");
    }

    @Test
    void deveFalharQuandoUrlLocalNaoFoiConfigurada() {
        JdbcPeopleAddressLocalReadAdapter adapter = new JdbcPeopleAddressLocalReadAdapter(
                new PeopleLocalReadModelSchemaMigrationProperties("", "sa", "", "org.h2.Driver", List.of()));

        assertThatThrownBy(() -> adapter.listarEnderecosPorPessoa(PESSOA_ID, ESCOLA_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("address-local-read-url-required");
    }

    private JdbcPeopleAddressLocalReadAdapter adapter(String url) {
        return new JdbcPeopleAddressLocalReadAdapter(
                new PeopleLocalReadModelSchemaMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaEPopular(String url, boolean duplicarPrincipal) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE pessoa (
                        id_pessoa UUID NOT NULL PRIMARY KEY,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE tipo_endereco (
                        id_tipo_endereco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(50) NOT NULL,
                        descricao VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE endereco (
                        id_endereco UUID NOT NULL PRIMARY KEY,
                        cep VARCHAR(20),
                        logradouro VARCHAR(150),
                        numero VARCHAR(30),
                        complemento VARCHAR(150),
                        bairro VARCHAR(100),
                        cidade VARCHAR(100),
                        uf VARCHAR(2),
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_endereco (
                        id_pessoa_endereco UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_endereco UUID NOT NULL,
                        id_tipo_endereco UUID NOT NULL,
                        principal BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, id_escola, nome_completo) VALUES
                    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '00000000-0000-0000-0000-000000000047', 'Ana Aluna')
                    """);
            statement.execute("""
                    INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES
                    ('11111111-1111-1111-1111-111111111111', 'RESIDENCIAL', 'Residencial')
                    """);
            statement.execute("""
                    INSERT INTO endereco (
                        id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at, updated_at
                    ) VALUES
                    (
                        '22222222-2222-2222-2222-222222222222',
                        '01001000',
                        'Praca da Se',
                        '100',
                        'Apto 1',
                        'Se',
                        'Sao Paulo',
                        'SP',
                        TIMESTAMP '2026-01-02 10:00:00',
                        TIMESTAMP '2026-01-02 10:00:00'
                    ),
                    (
                        '33333333-3333-3333-3333-333333333333',
                        '20040002',
                        'Rua da Assembleia',
                        '200',
                        NULL,
                        'Centro',
                        'Rio de Janeiro',
                        'RJ',
                        TIMESTAMP '2026-01-03 10:00:00',
                        TIMESTAMP '2026-01-03 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                    ) VALUES
                    (
                        '44444444-4444-4444-4444-444444444444',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '22222222-2222-2222-2222-222222222222',
                        '11111111-1111-1111-1111-111111111111',
                        TRUE,
                        TIMESTAMP '2026-01-04 10:00:00'
                    ),
                    (
                        '55555555-5555-5555-5555-555555555555',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '33333333-3333-3333-3333-333333333333',
                        '11111111-1111-1111-1111-111111111111',
                        %s,
                        TIMESTAMP '2026-01-03 10:00:00'
                    )
                    """.formatted(duplicarPrincipal ? "TRUE" : "FALSE"));
        }
    }
}
