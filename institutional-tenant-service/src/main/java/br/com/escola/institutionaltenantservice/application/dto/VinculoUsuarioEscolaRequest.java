package br.com.escola.institutionaltenantservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record VinculoUsuarioEscolaRequest(
        @NotNull UUID usuarioId,
        @NotNull UUID escolaId) {
}
