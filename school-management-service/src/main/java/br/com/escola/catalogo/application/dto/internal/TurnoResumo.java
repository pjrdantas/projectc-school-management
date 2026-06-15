package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record TurnoResumo(
        UUID id,
        String codigo,
        String descricao
) {
}
