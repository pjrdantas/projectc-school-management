package br.com.escola.peopleservice.application.context;

import java.util.Objects;
import java.util.UUID;

public record InternalRequestContext(
        String correlationId,
        UUID usuarioId,
        UUID escolaId) {

    public InternalRequestContext {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId nao pode ser vazio");
        }
        correlationId = correlationId.trim();
        usuarioId = Objects.requireNonNull(usuarioId, "usuarioId nao pode ser nulo");
        escolaId = Objects.requireNonNull(escolaId, "escolaId nao pode ser nulo");
    }
}
