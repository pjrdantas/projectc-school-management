package br.com.escola.identityaccessservice.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "login e obrigatorio") String login,
        @NotBlank(message = "senha e obrigatoria") String senha) {
}
