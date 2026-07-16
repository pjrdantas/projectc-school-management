package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelMigrationProperties;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelSourceProperties;

class JdbcResponsiblesReadModelSyncAdapterTest {

    @Test
    void deveExecutarBackfillControladoDasTabelasResponsavelEAlunoResponsavel() throws Exception {
        String sourceUrl = h2Url("responsibles_sync_source_" + UUID.randomUUID());
        String targetUrl = h2Url("responsibles_sync_target_" + UUID.randomUUID());
        criarSchemaOrigem(sourceUrl);
        criarSchemaDestino(targetUrl);

        JdbcResponsiblesReadModelSyncAdapter adapter = new JdbcResponsiblesReadModelSyncAdapter(
                new ResponsiblesReadModelSourceProperties(sourceUrl, "sa", "", "org.h2.Driver"),
                new ResponsiblesReadModelMigrationProperties(targetUrl, "sa", "", "org.h2.Driver", List.of()));

        var reports = adapter.synchronize(true, 100);

        assertThat(reports).hasSize(2);
        assertThat(reports).allMatch(report -> report.status().equals("success"));
        assertThat(reports.get(0).table()).isEqualTo("responsavel");
        assertThat(reports.get(0).backfilledRecords()).isEqualTo(1);
        assertThat(reports.get(1).table()).isEqualTo("aluno_responsavel");
        assertThat(reports.get(1).backfilledRecords()).isEqualTo(1);
        assertThat(contarLinhasDestino(targetUrl, "responsavel")).isEqualTo(1);
        assertThat(contarLinhasDestino(targetUrl, "aluno_responsavel")).isEqualTo(1);
    }

    @Test
    void deveBloquearQuandoUrlsDeOrigemOuDestinoNaoForemInformadas() {
        JdbcResponsiblesReadModelSyncAdapter adapter = new JdbcResponsiblesReadModelSyncAdapter(
                new ResponsiblesReadModelSourceProperties("", "", "", ""),
                new ResponsiblesReadModelMigrationProperties("", "", "", "", List.of()));

        var reports = adapter.synchronize(true, 100);

        assertThat(reports).hasSize(2);
        assertThat(reports).allMatch(report -> report.status().equals("blocked"));
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaOrigem(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
            statement.execute("""
                    CREATE TABLE pessoa (
                        id_pessoa UUID PRIMARY KEY,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        rg VARCHAR(20),
                        id_escola UUID NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE responsavel (
                        id_responsavel UUID PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID PRIMARY KEY,
                        id_aluno UUID NOT NULL,
                        id_responsavel UUID NOT NULL,
                        id_parentesco UUID,
                        responsavel_financeiro BOOLEAN NOT NULL,
                        responsavel_pedagogico BOOLEAN NOT NULL,
                        autorizado_retirar BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE endereco (
                        id_endereco UUID PRIMARY KEY,
                        cep VARCHAR(14),
                        logradouro VARCHAR(200),
                        numero VARCHAR(20),
                        complemento VARCHAR(120),
                        bairro VARCHAR(120),
                        cidade VARCHAR(120),
                        uf VARCHAR(2)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_endereco (
                        id_pessoa_endereco UUID PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_endereco UUID NOT NULL,
                        principal BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO escola (id_escola, nome) VALUES
                    ('00000000-0000-0000-0000-000000000047', 'Escola padrao')
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, telefone, rg, id_escola) VALUES
                    ('00000000-0000-0000-0000-000000000701', 'Maria Souza', '98765432100', 'maria@example.com', '11988887777', '1234567', '00000000-0000-0000-0000-000000000047')
                    """);
            statement.execute("""
                    INSERT INTO responsavel (id_responsavel, id_pessoa, created_at) VALUES
                    ('00000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000701', TIMESTAMP '2026-07-16 10:00:00')
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                        responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000501',
                        '00000000-0000-0000-0000-000000000401',
                        '00000000-0000-0000-0000-000000000601',
                        '00000000-0000-0000-0000-000000000301',
                        TRUE,
                        FALSE,
                        TRUE,
                        TIMESTAMP '2026-07-16 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO endereco (id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf) VALUES
                    ('00000000-0000-0000-0000-000000000801', '01001000', 'Rua Central', '100', 'Casa', 'Centro', 'Sao Paulo', 'SP')
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (id_pessoa_endereco, id_pessoa, id_endereco, principal) VALUES
                    ('00000000-0000-0000-0000-000000000901', '00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000801', TRUE)
                    """);
        }
    }

    private void criarSchemaDestino(String url) throws SQLException {
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
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL,
                        id_responsavel UUID NOT NULL,
                        id_parentesco UUID,
                        responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
                        responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE,
                        autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL,
                        CONSTRAINT fk_responsavel
                            FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel),
                        CONSTRAINT uk_aluno_responsavel
                            UNIQUE (id_aluno, id_responsavel)
                    )
                    """);
        }
    }

    private int contarLinhasDestino(String url, String tabela) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tabela);
                var resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
