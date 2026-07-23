package br.com.escola.catalog.interfaces.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTurmaRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Min(1) int capacidade,
        @NotNull UUID periodoLetivoId,
        @NotNull UUID serieId,
        @NotNull UUID turnoId,
        boolean ativo) {
}
