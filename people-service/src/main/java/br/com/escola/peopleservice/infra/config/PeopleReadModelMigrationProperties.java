package br.com.escola.peopleservice.infra.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.read-model.schema-migration")
public record PeopleReadModelMigrationProperties(
        String url,
        String username,
        String password,
        String driverClassName,
        List<String> locations) {

    public PeopleReadModelMigrationProperties {
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
            locations = List.of("classpath:db/people-readmodel/migration");
        }
    }
}

