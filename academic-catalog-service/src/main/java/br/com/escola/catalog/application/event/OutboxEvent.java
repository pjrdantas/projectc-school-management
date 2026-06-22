package br.com.escola.catalog.application.event;

import java.util.Objects;
import java.util.UUID;

public record OutboxEvent(
        String aggregateType,
        UUID aggregateId,
        IntegrationEventEnvelope envelope) {

    public OutboxEvent {
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException("aggregateType nao pode ser vazio");
        }
        aggregateType = aggregateType.trim();
        aggregateId = Objects.requireNonNull(aggregateId, "aggregateId nao pode ser nulo");
        envelope = Objects.requireNonNull(envelope, "envelope nao pode ser nulo");
    }
}
