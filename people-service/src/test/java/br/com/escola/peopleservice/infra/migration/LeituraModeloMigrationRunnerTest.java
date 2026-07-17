package br.com.escola.peopleservice.infra.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.escola.peopleservice.application.service.LeituraModeloMigrationState;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class LeituraModeloMigrationRunnerTest {

    @Test
    void naoExecutaMigrationQuandoFlagEstaDesligada() {
        LeituraModeloMigrationState state = new LeituraModeloMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new LeituraModeloMigrationRunner(
                new LeituraModeloProperties(false, false, false, false, false, false, 500, true),
                new LeituraModeloMigrationProperties("", "", "", "", List.of()),
                state,
                meterRegistry);

        runner.run(new DefaultApplicationArguments());

        assertThat(state.currentReport().status()).isEqualTo("disabled");
        assertThat(state.currentReport().executed()).isFalse();
        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    void bloqueiaMigrationHabilitadaSemUrlSemDerrubarQuandoFailOnErrorDesligado() {
        LeituraModeloMigrationState state = new LeituraModeloMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new LeituraModeloMigrationRunner(
                new LeituraModeloProperties(false, true, false, false, false, false, 500, true),
                new LeituraModeloMigrationProperties("", "", "", "", List.of()),
                state,
                meterRegistry);

        runner.run(new DefaultApplicationArguments());

        assertThat(state.currentReport().status()).isEqualTo("blocked");
        assertThat(state.currentReport().reason()).isEqualTo("schema-migration-url-required");
        assertThat(meterRegistry.counter(
                "people.readmodel.migrations",
                "status", "failure",
                "reason", "missing-url").count()).isEqualTo(1.0d);
    }

    @Test
    void falhaMigrationHabilitadaSemUrlQuandoFailOnErrorLigado() {
        var runner = new LeituraModeloMigrationRunner(
                new LeituraModeloProperties(false, true, false, true, false, false, 500, true),
                new LeituraModeloMigrationProperties("", "", "", "", List.of()),
                new LeituraModeloMigrationState(),
                new SimpleMeterRegistry());

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("schema-migration-url-required");
    }

    @Test
    void aplicaMigrationsReadOnlyDosCatalogosIdentidadeEConsultaCadastroQuandoOptInEstaHabilitado()
            throws Exception {
        String url = "jdbc:h2:mem:people_schema_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        LeituraModeloMigrationState state = new LeituraModeloMigrationState();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var runner = new LeituraModeloMigrationRunner(
                new LeituraModeloProperties(false, true, false, true, false, false, 500, true),
                new LeituraModeloMigrationProperties(
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
                .containsExactly(
                        "tipo_pessoa",
                        "tipo_endereco",
                        "status_aluno",
                        "parentesco",
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "aluno",
                        "responsavel",
                        "aluno_responsavel",
                        "endereco",
                        "pessoa_endereco",
                        "people_funcionario_read_model",
                        "people_professor_read_model");
        assertThat(meterRegistry.counter(
                "people.readmodel.migrations",
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
                var resultSet = connection.getMetaData().getTables(null, null, "status_aluno", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "parentesco", null)) {
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
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "aluno", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "responsavel", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "aluno_responsavel", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "endereco", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "pessoa_endereco", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "people_funcionario_read_model", null)) {
            assertThat(resultSet.next()).isTrue();
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var resultSet = connection.getMetaData().getTables(null, null, "people_professor_read_model", null)) {
            assertThat(resultSet.next()).isTrue();
        }
    }
}


