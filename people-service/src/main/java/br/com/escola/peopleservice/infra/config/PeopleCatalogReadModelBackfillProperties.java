package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.shadow.local-persistence.catalog-backfill")
public record PeopleCatalogReadModelBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        String sourceDriverClassName) {

    public PeopleCatalogReadModelBackfillProperties {
        sourceUrl = sourceUrl == null ? "" : sourceUrl;
        sourceUsername = sourceUsername == null ? "" : sourceUsername;
        sourcePassword = sourcePassword == null ? "" : sourcePassword;
        sourceDriverClassName = sourceDriverClassName == null ? "" : sourceDriverClassName;
    }
}
