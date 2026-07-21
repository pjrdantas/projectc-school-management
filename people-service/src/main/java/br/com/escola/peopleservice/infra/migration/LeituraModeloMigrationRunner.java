package br.com.escola.peopleservice.infra.migration;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.state.LeituraModeloMigrationSummary;
import br.com.escola.peopleservice.application.service.LeituraModeloMigrationState;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LeituraModeloMigrationRunner implements ApplicationRunner, Ordered {

    private static final List<String> TABLES = List.of(
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

    private final LeituraModeloProperties readModelProperties;
    private final LeituraModeloMigrationProperties migrationProperties;
    private final LeituraModeloMigrationState migrationState;
    private final MeterRegistry meterRegistry;

    public LeituraModeloMigrationRunner(
            LeituraModeloProperties readModelProperties,
            LeituraModeloMigrationProperties migrationProperties,
            LeituraModeloMigrationState migrationState,
            MeterRegistry meterRegistry) {
        this.readModelProperties = readModelProperties;
        this.migrationProperties = migrationProperties;
        this.migrationState = migrationState;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!readModelProperties.migrationEnabled()) {
            migrationState.update(report(false, false, true, "disabled", "schema-migration-disabled", 0));
            return;
        }

        if (!StringUtils.hasText(migrationProperties.url())) {
            registrarFalha("missing-url");
            migrationState.update(report(true, false, false, "blocked", "schema-migration-url-required", 0));
            falharSeConfigurado("schema-migration-url-required");
            return;
        }

        try {
            carregarDriverSeConfigurado();
            var result = Flyway.configure()
                    .dataSource(
                            migrationProperties.url(),
                            migrationProperties.username(),
                            migrationProperties.password())
                    .locations(migrationProperties.locations().toArray(String[]::new))
                    .load()
                    .migrate();
            registrarSucesso();
            migrationState.update(report(
                    true,
                    true,
                    true,
                    "applied",
                    "schema-migration-applied",
                    result.migrationsExecuted));
        } catch (RuntimeException | ClassNotFoundException exception) {
            registrarFalha("execution-error");
            migrationState.update(report(true, true, false, "failed", exception.getClass().getSimpleName(), 0));
            falharSeConfigurado(exception);
        }
    }

    private void carregarDriverSeConfigurado() throws ClassNotFoundException {
        if (StringUtils.hasText(migrationProperties.driverClassName())) {
            Class.forName(migrationProperties.driverClassName());
        }
    }

    private LeituraModeloMigrationSummary report(
            boolean enabled,
            boolean executed,
            boolean success,
            String status,
            String reason,
            int migrationsExecuted) {
        return new LeituraModeloMigrationSummary(
                enabled,
                executed,
                success,
                status,
                reason,
                migrationProperties.locations(),
                TABLES,
                migrationsExecuted);
    }

    private void registrarSucesso() {
        Counter.builder("people.readmodel.migrations")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
    }

    private void registrarFalha(String reason) {
        Counter.builder("people.readmodel.migrations")
                .tag("status", "failure")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    private void falharSeConfigurado(String reason) {
        if (readModelProperties.failOnError()) {
            throw new IllegalStateException(reason);
        }
    }

    private void falharSeConfigurado(Exception exception) {
        if (readModelProperties.failOnError()) {
            throw new IllegalStateException("schema-migration-failed", exception);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}


