package br.com.escola.accesscontrol.adapter.in.web.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String username,
        String nome
) {}
