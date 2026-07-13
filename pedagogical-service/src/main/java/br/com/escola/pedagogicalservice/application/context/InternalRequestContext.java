package br.com.escola.pedagogicalservice.application.context;

import java.util.UUID;

public record InternalRequestContext(
        String correlationId,
        UUID usuarioId,
        UUID escolaId) {
}
