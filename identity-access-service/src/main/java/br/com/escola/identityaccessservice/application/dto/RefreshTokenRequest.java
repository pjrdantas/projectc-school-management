package br.com.escola.identityaccessservice.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken e obrigatorio") String refreshToken) {
}
