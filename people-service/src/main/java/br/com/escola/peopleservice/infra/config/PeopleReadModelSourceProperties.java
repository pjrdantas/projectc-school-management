package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.read-model.source")
public record PeopleReadModelSourceProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        String sourceDriverClassName) {

    public PeopleReadModelSourceProperties {
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        sourceUsername = sourceUsername == null ? "" : sourceUsername;
        sourcePassword = sourcePassword == null ? "" : sourcePassword;
        sourceDriverClassName = sourceDriverClassName == null ? "" : sourceDriverClassName;
    }
}

