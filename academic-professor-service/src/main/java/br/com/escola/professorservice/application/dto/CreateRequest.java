package br.com.escola.professorservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequest(
        @NotNull UUID funcionarioId,
        @Size(max = 80) String registroProfissional,
        @Size(max = 150) String formacao,
        Boolean ativo
) {
}

