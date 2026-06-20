package br.com.escola.bff.application.context;

import java.util.UUID;

public record RequestContext(
        String correlationId,
        UUID usuarioId,
        UUID escolaId) {

    public static final String REACTOR_CONTEXT_KEY = RequestContext.class.getName();
}
