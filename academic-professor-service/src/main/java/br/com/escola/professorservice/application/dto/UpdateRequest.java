package br.com.escola.professorservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequest(
        @Size(max = 80) String registroProfissional,
        @Size(max = 150) String formacao,
        @NotNull Boolean ativo) {
}
