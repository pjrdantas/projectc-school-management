package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("responsibles.persistence.backfill")
public record ResponsiblesWriteBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        int batchSize,
        boolean failOnMismatch) {

    public ResponsiblesWriteBackfillProperties {
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
