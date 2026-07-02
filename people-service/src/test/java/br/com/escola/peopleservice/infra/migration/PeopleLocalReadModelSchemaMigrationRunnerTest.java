package br.com.escola.peopleservice.infra.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.escola.peopleservice.application.service.PeopleLocalReadModelSchemaMigrationState;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalReadModelSchemaMigrationRunnerTest {

    @Test
    void naoExecutaMigrationQuandoFlagEstaDesligada() {
        PeopleLocalReadModelSchemaMigrationState state = new PeopleLocalReadModelSchemaMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new PeopleLocalReadModelSchemaMigrationRunner(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                new PeopleLocalReadModelSchemaMigrationProperties("", "", "", "", List.of()),
                state,
                meterRegistry);

        runner.run(new DefaultApplicationArguments());

        assertThat(state.currentReport().status()).isEqualTo("disabled");
        assertThat(state.currentReport().executed()).isFalse();
        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    void bloqueiaMigrationHabilitadaSemUrlSemDerrubarQuandoFailOnErrorDesligado() {
        PeopleLocalReadModelSchemaMigrationState state = new PeopleLocalReadModelSchemaMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new PeopleLocalReadModelSchemaMigrationRunner(
                new PeopleLocalPersistenceProperties(false, true, false, false, false, false, 500, true),
                new PeopleLocalReadModelSchemaMigrationProperties("", "", "", "", List.of()),
                state,
                meterRegistry);

        runner.run(new DefaultApplicationArguments());

        assertThat(state.currentReport().status()).isEqualTo("blocked");
        assertThat(state.currentReport().reason()).isEqualTo("schema-migration-url-required");
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.schema.migrations",
                "status", "failure",
                "reason", "missing-url").count()).isEqualTo(1.0d);
    }

    @Test
    void falhaMigrationHabilitadaSemUrlQuandoFailOnErrorLigado() {
        var runner = new PeopleLocalReadModelSchemaMigrationRunner(
                new PeopleLocalPersistenceProperties(false, true, false, true, false, false, 500, true),
                new PeopleLocalReadModelSchemaMigrationProperties("", "", "", "", List.of()),
                new PeopleLocalReadModelSchemaMigrationState(),
                new SimpleMeterRegistry());

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("schema-migration-url-required");
    }

    @Test
    void aplicaMigrationsReadOnlyDosCatalogosEIdentidadeQuandoOptInEstaHabilitado() throws Exception {
        String url = "jdbc:h2:mem:people_schema_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        PeopleLocalReadModelSchemaMigrationState state = new PeopleLocalReadModelSchemaMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new PeopleLocalReadModelSchemaMigrationRunner(
                new PeopleLocalPersistenceProperties(false, true, false, true, false, false, 500, true),
                new PeopleLocalReadModelSchemaMigrationProperties(
                        url,
                        "sa",
                        "",
                        "org.h2.Driver",
                        List.of("classpath:db/people-readmodel/migration")),
                state,
                meterRegistry);

        runner.run(new DefaultApplicationArguments());

        assertThat(state.currentReport().status()).isEqualTo("applied");
        assertThat(state.currentReport().success()).isTrue();
        assertThat(state.currentReport().tables())
                .containsExactly("tipo_pessoa", "tipo_endereco", "pessoa", "pessoa_tipo_pessoa");
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.schema.migrations",
                "status", "success").count()).isEqualTo(1.0d);

        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "tipo_pessoa", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "tipo_endereco", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "pessoa", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "pessoa_tipo_pessoa", null)) {
            assertThat(resultSet.next()).isTrue();
        }
    }
}
