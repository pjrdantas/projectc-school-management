package br.com.escola.responsiblesservice.infra.migration;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelMigrationProperties;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Component
public class ResponsiblesReadModelMigrationRunner implements ApplicationRunner {

    private final ResponsiblesReadModelProperties readModelProperties;
    private final ResponsiblesReadModelMigrationProperties migrationProperties;

    public ResponsiblesReadModelMigrationRunner(
            ResponsiblesReadModelProperties readModelProperties,
            ResponsiblesReadModelMigrationProperties migrationProperties) {
        this.readModelProperties = readModelProperties;
        this.migrationProperties = migrationProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!readModelProperties.enabled() || !readModelProperties.migrationEnabled()) {
            return;
        }
        if (!StringUtils.hasText(migrationProperties.url())) {
            failIfConfigured("responsibles-read-model-schema-migration-url-required");
            return;
        }
        if (StringUtils.hasText(migrationProperties.driverClassName())) {
            Class.forName(migrationProperties.driverClassName());
        }

        try {
            Flyway.configure()
                    .dataSource(
                            migrationProperties.url(),
                            migrationProperties.username(),
                            migrationProperties.password())
                    .locations(migrationProperties.locations().toArray(String[]::new))
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();
        } catch (RuntimeException exception) {
            failIfConfigured("responsibles-read-model-schema-migration-failed", exception);
        }
    }

    private void failIfConfigured(String message) {
        failIfConfigured(message, null);
    }

    private void failIfConfigured(String message, Exception cause) {
        if (readModelProperties.failOnError()) {
            throw cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
        }
    }
}
