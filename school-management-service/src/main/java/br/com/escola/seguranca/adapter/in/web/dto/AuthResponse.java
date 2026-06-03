package br.com.escola.seguranca.adapter.in.web.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String username,
        String nome,
        List<String> perfis,
        List<String> permissoes
) {}
