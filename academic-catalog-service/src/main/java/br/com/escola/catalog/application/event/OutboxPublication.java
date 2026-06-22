package br.com.escola.catalog.application.event;

import java.util.Objects;
import java.util.UUID;
import java.time.Instant;

public record OutboxPublication(
        String aggregateType,
        UUID aggregateId,
        IntegrationEventEnvelope envelope,
        int attempts,
        Instant claimToken) {

    public OutboxPublication {
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException("aggregateType nao pode ser vazio");
        }
        aggregateId = Objects.requireNonNull(aggregateId, "aggregateId nao pode ser nulo");
        envelope = Objects.requireNonNull(envelope, "envelope nao pode ser nulo");
        if (attempts < 0) {
            throw new IllegalArgumentException("attempts nao pode ser negativo");
        }
        claimToken = Objects.requireNonNull(claimToken, "claimToken nao pode ser nulo");
    }
}
