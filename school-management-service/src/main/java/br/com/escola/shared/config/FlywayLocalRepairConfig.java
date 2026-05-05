package br.com.escola.shared.config;

import org.flywaydb.core.api.exception.FlywayValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class FlywayLocalRepairConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FlywayLocalRepairConfig.class);

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                flyway.validate();
            } catch (FlywayValidateException exception) {
                LOG.warn("Flyway validation failed in local profile. Running repair before migrate.");
                flyway.repair();
            }

            flyway.migrate();
        };
    }
}
