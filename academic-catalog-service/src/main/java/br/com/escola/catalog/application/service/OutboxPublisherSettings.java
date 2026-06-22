package br.com.escola.catalog.application.service;

import java.time.Duration;

public record OutboxPublisherSettings(
        int batchSize,
        int maxAttempts,
        Duration lockTimeout,
        Duration initialBackoff,
        Duration maxBackoff,
        String topic,
        String dltTopic) {

    public OutboxPublisherSettings {
        if (batchSize <= 0 || maxAttempts <= 0) {
            throw new IllegalArgumentException("batchSize e maxAttempts devem ser positivos");
        }
        if (lockTimeout == null || lockTimeout.isNegative() || lockTimeout.isZero()
                || initialBackoff == null || initialBackoff.isNegative() || initialBackoff.isZero()
                || maxBackoff == null || maxBackoff.compareTo(initialBackoff) < 0) {
            throw new IllegalArgumentException("Duracoes do publisher sao invalidas");
        }
        if (topic == null || topic.isBlank() || dltTopic == null || dltTopic.isBlank()) {
            throw new IllegalArgumentException("Topicos do publisher sao obrigatorios");
        }
    }
}
