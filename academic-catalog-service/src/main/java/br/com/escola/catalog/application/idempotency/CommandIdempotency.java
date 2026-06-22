package br.com.escola.catalog.application.idempotency;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CommandIdempotency(
        UUID escolaId,
        String key,
        String requestHash,
        String resourceType,
        UUID resourceId,
        Instant createdAt) {

    public CommandIdempotency {
        escolaId = Objects.requireNonNull(escolaId, "escolaId nao pode ser nulo");
        key = required(key, "key");
        requestHash = required(requestHash, "requestHash");
        resourceType = required(resourceType, "resourceType");
        resourceId = Objects.requireNonNull(resourceId, "resourceId nao pode ser nulo");
        createdAt = Objects.requireNonNull(createdAt, "createdAt nao pode ser nulo");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " nao pode ser vazio");
        }
        return value.trim();
    }
}
