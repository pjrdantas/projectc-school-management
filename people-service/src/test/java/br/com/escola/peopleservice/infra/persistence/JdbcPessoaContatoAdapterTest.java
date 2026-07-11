package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

class JdbcPessoaContatoAdapterTest {

    @Test
    void deveLerContatoLocalPorPessoaEEscola() throws Exception {
        String url = h2Url("contact_local_read_" + UUID.randomUUID());
        UUID pessoaId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcPessoaContatoAdapter adapter = new JdbcPessoaContatoAdapter(
                new PeopleReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarContatoPorPessoa(pessoaId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.get().pessoaId()).isEqualTo(pessoaId);
        assertThat(response.get().escolaId()).isEqualTo(escolaId);
        assertThat(response.get().email()).isEqualTo("ana.aluna@example.com");
        assertThat(response.get().telefone()).isEqualTo("11999999999");
        assertThat(response.get().ativo()).isTrue();
    }

    @Test
    void naoRetornaContatoDeOutraEscola() throws Exception {
        String url = h2Url("contact_local_read_" + UUID.randomUUID());
        UUID pessoaId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        criarSchemaEPopular(url);
        JdbcPessoaContatoAdapter adapter = new JdbcPessoaContatoAdapter(
                new PeopleReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarContatoPorPessoa(
                pessoaId,
                UUID.fromString("00000000-0000-0000-0000-000000000099"));

        assertThat(response).isEmpty();
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaEPopular(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE pessoa (
                        id_pessoa UUID NOT NULL PRIMARY KEY,
                        id_escola UUID NOT NULL,
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        ativo BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, id_escola, email, telefone, ativo) VALUES
                    (
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '00000000-0000-0000-0000-000000000047',
                        'ana.aluna@example.com',
                        '11999999999',
                        TRUE
                    )
                    """);
        }
    }
}

