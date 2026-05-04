package br.com.escola.accesscontrol.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "login é obrigatório")
        String login,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {}
