package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("people.persistence.backfill")
public record PeopleWriteBackfillProperties(
        boolean enabled,
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        int batchSize,
        boolean failOnMismatch) {

    public PeopleWriteBackfillProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
        if (sourceUsername == null) {
            sourceUsername = "";
        }
        if (sourcePassword == null) {
            sourcePassword = "";
        }
    }
}
