package br.com.escola.identityaccessservice.application.dto;

import java.util.UUID;

public record AuthContextResponse(
        UUID usuarioId,
        UUID escolaId,
        String escolaNome,
        String username) {
}
