package br.com.escola.enrollmentdocumentservice.application.context;

import java.util.UUID;

public record InternalRequestContext(
        String correlationId,
        UUID usuarioId,
        UUID escolaId) {
}
