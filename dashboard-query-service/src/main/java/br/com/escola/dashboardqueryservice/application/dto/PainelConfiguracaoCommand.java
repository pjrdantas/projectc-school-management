package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PainelConfiguracaoCommand(
        @NotNull UUID publicoId,
        @NotBlank @Size(max = 80) String codigo,
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 4000) String descricao,
        Boolean ativo) {
}
