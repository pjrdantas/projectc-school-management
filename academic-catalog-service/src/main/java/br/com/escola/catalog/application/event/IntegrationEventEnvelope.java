package br.com.escola.catalog.application.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record IntegrationEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        UUID causationId,
        UUID usuarioId,
        UUID escolaId,
        Map<String, Object> payload) {

    public IntegrationEventEnvelope {
        Objects.requireNonNull(eventId, "eventId nao pode ser nulo");
        Objects.requireNonNull(eventType, "eventType nao pode ser nulo");
        Objects.requireNonNull(occurredAt, "occurredAt nao pode ser nulo");
        Objects.requireNonNull(correlationId, "correlationId nao pode ser nulo");
        Objects.requireNonNull(escolaId, "escolaId nao pode ser nulo");
        payload = Map.copyOf(Objects.requireNonNull(payload, "payload nao pode ser nulo"));
    }
}
