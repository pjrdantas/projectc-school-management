package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleCatalogReadModelBackfillProperties;
import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;

class JdbcPeopleCatalogReadModelSyncAdapterTest {

    @Test
    void bloqueiaSincronizacaoQuandoOrigemOuDestinoNaoEstaoConfigurados() {
        JdbcPeopleCatalogReadModelSyncAdapter adapter = new JdbcPeopleCatalogReadModelSyncAdapter(
                new PeopleCatalogReadModelBackfillProperties("", "", "", ""),
                new PeopleLocalReadModelSchemaMigrationProperties("", "", "", "", List.of()));

        var reports = adapter.synchronize(true, true, 100);

        assertThat(reports)
                .hasSize(2)
                .allSatisfy(report -> {
                    assertThat(report.status()).isEqualTo("blocked");
                    assertThat(report.reason()).isEqualTo("catalog-backfill-source-url-required");
                    assertThat(report.backfillPlanned()).isTrue();
                    assertThat(report.reconciliationPlanned()).isTrue();
                });
    }

    @Test
    void copiaEReconciliaCatalogosDoMonolitoParaSchemaLocal() throws Exception {
        String sourceUrl = h2Url("source_" + UUID.randomUUID());
        String targetUrl = h2Url("target_" + UUID.randomUUID());
        criarSchema(sourceUrl);
        criarSchema(targetUrl);
        popularOrigem(sourceUrl);

        JdbcPeopleCatalogReadModelSyncAdapter adapter = new JdbcPeopleCatalogReadModelSyncAdapter(
                new PeopleCatalogReadModelBackfillProperties(sourceUrl, "sa", "", "org.h2.Driver"),
                new PeopleLocalReadModelSchemaMigrationProperties(targetUrl, "sa", "", "org.h2.Driver", List.of()));

        var reports = adapter.synchronize(true, true, 100);

        assertThat(reports)
                .hasSize(2)
                .allSatisfy(report -> {
                    assertThat(report.status()).isEqualTo("success");
                    assertThat(report.reason()).isEqualTo("catalog-sync-completed");
                    assertThat(report.divergences()).isZero();
                    assertThat(report.backfilledRecords()).isGreaterThan(0);
                });
        assertThat(reports)
                .extracting("table")
                .containsExactly("tipo_pessoa", "tipo_endereco");
        assertThat(reports.stream().mapToInt(report -> report.sourceRows()).sum()).isEqualTo(5);
        assertThat(reports.stream().mapToInt(report -> report.targetRows()).sum()).isEqualTo(5);

        assertThat(contar(targetUrl, "tipo_pessoa")).isEqualTo(3);
        assertThat(contar(targetUrl, "tipo_endereco")).isEqualTo(2);
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchema(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE tipo_pessoa (
                        id_tipo_pessoa UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE tipo_endereco (
                        id_tipo_endereco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
        }
    }

    private void popularOrigem(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at) VALUES
                    ('11111111-1111-1111-1111-111111111111', 'ALUNO', 'Aluno', CURRENT_TIMESTAMP),
                    ('22222222-2222-2222-2222-222222222222', 'RESPONSAVEL', 'Responsavel', CURRENT_TIMESTAMP),
                    ('33333333-3333-3333-3333-333333333333', 'PROFESSOR', 'Professor', CURRENT_TIMESTAMP)
                    """);
            statement.execute("""
                    INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES
                    ('44444444-4444-4444-4444-444444444444', 'RESIDENCIAL', 'Residencial'),
                    ('55555555-5555-5555-5555-555555555555', 'COMERCIAL', 'Comercial')
                    """);
        }
    }

    private int contar(String url, String table) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
