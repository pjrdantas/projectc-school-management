package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "responsibles.read-model.source")
public record ResponsiblesReadModelSourceProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        String sourceDriverClassName) {

    public ResponsiblesReadModelSourceProperties {
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        sourceUsername = sourceUsername == null ? "" : sourceUsername;
        sourcePassword = sourcePassword == null ? "" : sourcePassword;
        sourceDriverClassName = sourceDriverClassName == null ? "" : sourceDriverClassName;
    }
}
