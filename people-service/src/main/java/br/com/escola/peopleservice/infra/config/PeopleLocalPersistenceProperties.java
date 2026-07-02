package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.shadow.local-persistence")
public record PeopleLocalPersistenceProperties(
        boolean enabled,
        boolean migrationEnabled,
        boolean readModelCutoverEnabled,
        boolean failOnError) {
}
