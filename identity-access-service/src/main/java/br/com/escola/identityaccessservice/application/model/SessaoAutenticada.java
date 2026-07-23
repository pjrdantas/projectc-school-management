package br.com.escola.identityaccessservice.application.model;

import java.util.List;
import java.util.UUID;

public record SessaoAutenticada(
        String accessToken,
        String refreshToken,
        UUID usuarioId,
        String username,
        String nome,
        List<String> perfis,
        List<String> permissoes) {
}
