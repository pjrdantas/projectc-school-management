package br.com.escola.accesscontrol.adapter.in.web.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken é obrigatório")
        String refreshToken
) {}
