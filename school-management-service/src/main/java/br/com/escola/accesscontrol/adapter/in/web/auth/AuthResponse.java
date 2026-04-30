package br.com.escola.accesscontrol.adapter.in.web.auth;

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
