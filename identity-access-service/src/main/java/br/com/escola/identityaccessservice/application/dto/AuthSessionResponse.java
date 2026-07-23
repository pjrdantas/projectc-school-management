package br.com.escola.identityaccessservice.application.dto;

import java.util.List;
import java.util.UUID;

public record AuthSessionResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID usuarioId,
        UUID professorId,
        String username,
        String login,
        String nome,
        List<String> perfis,
        List<String> permissoes) {
}
