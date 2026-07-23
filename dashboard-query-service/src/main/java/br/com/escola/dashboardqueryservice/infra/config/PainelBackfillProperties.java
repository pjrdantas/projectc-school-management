package br.com.escola.dashboardqueryservice.infra.config;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("dashboard-query.persistence.backfill")
public record PainelBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        UUID escolaId,
        int batchSize,
        boolean failOnMismatch) {

    public PainelBackfillProperties {
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
