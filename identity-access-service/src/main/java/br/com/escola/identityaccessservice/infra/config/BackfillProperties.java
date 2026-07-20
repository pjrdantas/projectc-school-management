package br.com.escola.identityaccessservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identity-access.persistence.backfill")
public record BackfillProperties(
        boolean enabled,
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        int batchSize,
        boolean failOnMismatch) {

    public BackfillProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
    }
}
