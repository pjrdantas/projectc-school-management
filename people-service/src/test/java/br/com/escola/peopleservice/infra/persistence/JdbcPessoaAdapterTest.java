package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

class JdbcPessoaAdapterTest {

    @Test
    void deveLerPessoaLocalPorIdEEscola() throws Exception {
        String url = h2Url("identity_local_read_" + UUID.randomUUID());
        UUID pessoaId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcPessoaAdapter adapter = new JdbcPessoaAdapter(
                new LeituraModeloMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarPessoaPorId(pessoaId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.get().id()).isEqualTo(pessoaId);
        assertThat(response.get().nomeCompleto()).isEqualTo("Ana Aluna");
        assertThat(response.get().escolaId()).isEqualTo(escolaId);
        assertThat(response.get().escolaNome()).isEqualTo("Escola Principal");
        assertThat(response.get().ativo()).isTrue();
    }

    @Test
    void naoRetornaPessoaDeOutraEscola() throws Exception {
        String url = h2Url("identity_local_read_" + UUID.randomUUID());
        UUID pessoaId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        criarSchemaEPopular(url);
        JdbcPessoaAdapter adapter = new JdbcPessoaAdapter(
                new LeituraModeloMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarPessoaPorId(
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
                        escola_nome VARCHAR(150) NOT NULL,
                        nome_completo VARCHAR(150) NOT NULL,
                        ativo BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, id_escola, escola_nome, nome_completo, ativo) VALUES
                    (
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '00000000-0000-0000-0000-000000000047',
                        'Escola Principal',
                        'Ana Aluna',
                        TRUE
                    )
                    """);
        }
    }
}


