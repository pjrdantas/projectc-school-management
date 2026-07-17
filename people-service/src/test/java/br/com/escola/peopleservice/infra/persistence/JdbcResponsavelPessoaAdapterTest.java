package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

class JdbcResponsavelPessoaAdapterTest {

    @Test
    void deveLerVinculoResponsavelPessoaPorResponsavelEEscola() throws Exception {
        String url = h2Url("responsible_pessoa_local_read_" + UUID.randomUUID());
        UUID responsavelId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID pessoaId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcResponsavelPessoaAdapter adapter = new JdbcResponsavelPessoaAdapter(
                new LeituraModeloMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarVinculoPorResponsavelId(responsavelId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.get().responsavelId()).isEqualTo(responsavelId);
        assertThat(response.get().pessoaId()).isEqualTo(pessoaId);
        assertThat(response.get().escolaId()).isEqualTo(escolaId);
    }

    @Test
    void naoRetornaVinculoQuandoEscolaDiverge() throws Exception {
        String url = h2Url("responsible_pessoa_local_read_" + UUID.randomUUID());
        UUID responsavelId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        criarSchemaEPopular(url);
        JdbcResponsavelPessoaAdapter adapter = new JdbcResponsavelPessoaAdapter(
                new LeituraModeloMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarVinculoPorResponsavelId(
                responsavelId,
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
                    CREATE TABLE responsavel (
                        id_responsavel UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO responsavel (id_responsavel, id_pessoa, id_escola, nome_completo) VALUES
                    (
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '00000000-0000-0000-0000-000000000047',
                        'Rita Responsavel'
                    )
                    """);
        }
    }
}


