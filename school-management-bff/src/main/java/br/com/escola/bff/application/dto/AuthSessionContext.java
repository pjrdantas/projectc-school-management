package br.com.escola.bff.application.dto;

import java.util.UUID;

public record AuthSessionContext(
        UUID usuarioId,
        UUID escolaId,
        String escolaNome
) {

    public AuthSessionContext(UUID usuarioId, UUID escolaId) {
        this(usuarioId, escolaId, null);
    }
}
