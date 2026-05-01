package br.com.projeto.piloto.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
    @NotBlank(message = "refreshToken é obrigatório") String refreshToken
) {}
