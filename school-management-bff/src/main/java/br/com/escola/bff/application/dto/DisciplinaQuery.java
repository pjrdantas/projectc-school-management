package br.com.escola.bff.application.dto;

import java.util.Objects;

public record DisciplinaQuery(
        String authorization,
        String correlationId) {

    public DisciplinaQuery {
        Objects.requireNonNull(authorization, "authorization nao pode ser nulo");
        Objects.requireNonNull(correlationId, "correlationId nao pode ser nulo");
    }
}

