package br.com.escola.identityaccessservice.application.model;

import java.util.UUID;

public record ContextoSessaoAutenticada(
        UUID sessaoId,
        UUID usuarioId,
        UUID escolaId,
        String username) {
}
