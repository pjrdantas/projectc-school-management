package br.com.escola.seguranca.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "login é obrigatório")
        String login,

        @NotBlank(message = "senha é obrigatória")
        String senha,

        UUID escolaId
) {}
