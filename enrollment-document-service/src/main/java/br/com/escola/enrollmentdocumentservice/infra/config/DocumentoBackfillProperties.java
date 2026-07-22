package br.com.escola.enrollmentdocumentservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("enrollment-document.document-backfill")
public record DocumentoBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        String sourceStorageRoot,
        int batchSize,
        boolean failOnMismatch) {

    public DocumentoBackfillProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
        sourceUsername = sourceUsername == null ? "" : sourceUsername;
        sourcePassword = sourcePassword == null ? "" : sourcePassword;
    }
}
