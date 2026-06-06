package br.com.escola.seguranca.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID usuarioId,
        UUID professorId,
        String username,
        String nome,
        List<String> perfis,
        List<String> permissoes
) {}
