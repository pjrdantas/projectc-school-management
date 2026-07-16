package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelMigrationProperties;

class JdbcResponsavelReadModelAdapterTest {

    @Test
    void deveListarResponsaveisDoReadModelLocalComFiltrosMinimos() throws Exception {
        String url = h2Url("responsibles_read_list_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.listarResponsaveis(escolaId, "Maria", "98765432100");

        assertThat(response).isPresent();
        assertThat(response.get()).hasSize(1);
        assertThat(response.get().getFirst().nomeCompleto()).isEqualTo("Maria Souza");
        assertThat(response.get().getFirst().escolaNome()).isEqualTo("Escola padrao");
    }

    @Test
    void deveBuscarResponsavelPorIdNoReadModelLocal() throws Exception {
        String url = h2Url("responsibles_read_detail_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var response = adapter.buscarResponsavelPorId(responsavelId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.get().nomeCompleto()).isEqualTo("Maria Souza");
        assertThat(response.get().cpf()).isEqualTo("98765432100");
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
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        rg VARCHAR(20),
                        cep VARCHAR(14),
                        logradouro VARCHAR(200),
                        numero VARCHAR(20),
                        complemento VARCHAR(120),
                        bairro VARCHAR(120),
                        cidade VARCHAR(120),
                        uf VARCHAR(2),
                        id_escola UUID NOT NULL,
                        escola_nome VARCHAR(150),
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO responsavel (
                        id_responsavel, nome_completo, cpf, email, telefone, rg,
                        cep, logradouro, numero, complemento, bairro, cidade, uf,
                        id_escola, escola_nome, created_at
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000601',
                        'Maria Souza',
                        '98765432100',
                        'maria.souza@example.com',
                        '11988887777',
                        '1234567',
                        '01001000',
                        'Rua Central',
                        '100',
                        'Casa',
                        'Centro',
                        'Sao Paulo',
                        'SP',
                        '00000000-0000-0000-0000-000000000047',
                        'Escola padrao',
                        TIMESTAMP '2026-07-16 10:00:00'
                    )
                    """);
        }
    }
}
