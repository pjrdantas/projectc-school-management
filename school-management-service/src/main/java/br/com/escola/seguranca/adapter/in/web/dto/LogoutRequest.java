package br.com.escola.seguranca.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "refreshToken é obrigatório")
        String refreshToken
) {}
