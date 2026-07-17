package br.com.escola.responsiblesservice.infra.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "responsibles.read-model.schema-migration")
public record LeituraModeloMigrationProperties(
        String url,
        String username,
        String password,
        String driverClassName,
        List<String> locations) {

    public LeituraModeloMigrationProperties {
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        if (driverClassName == null) {
            driverClassName = "";
        }
        if (locations == null || locations.isEmpty()) {
            locations = List.of("classpath:db/responsibles-readmodel/migration");
        }
    }
}

