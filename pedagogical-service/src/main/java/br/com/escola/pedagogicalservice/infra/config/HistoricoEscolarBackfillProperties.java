package br.com.escola.pedagogicalservice.infra.config;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pedagogical.persistence.backfill")
public record HistoricoEscolarBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        UUID schoolId,
        int batchSize,
        boolean failOnMismatch) {

    public HistoricoEscolarBackfillProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
        sourceUsername = sourceUsername == null ? "" : sourceUsername;
        sourcePassword = sourcePassword == null ? "" : sourcePassword;
    }
}
