package br.com.escola.bff.application.dto;

import java.util.UUID;

public record TurnoResolved(
        UUID id,
        String codigo
) {
}
