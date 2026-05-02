package br.com.projeto.piloto.accesscontrol.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
    @NotBlank(message = "refreshToken é obrigatório") String refreshToken
) {}
