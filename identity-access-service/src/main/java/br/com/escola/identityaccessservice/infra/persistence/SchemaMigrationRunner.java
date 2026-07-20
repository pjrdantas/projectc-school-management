package br.com.escola.identityaccessservice.infra.persistence;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.infra.config.SchemaMigrationProperties;

@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private final SchemaMigrationProperties properties;

    public SchemaMigrationRunner(SchemaMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        Flyway.configure()
                .dataSource(properties.url(), properties.username(), properties.password())
                .locations(properties.locations())
                .load()
                .migrate();
    }
}
