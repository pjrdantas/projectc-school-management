package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PainelWidgetCommand(
        @NotNull UUID painelId,
        @NotBlank @Size(max = 80) String codigo,
        @NotBlank @Size(max = 150) String titulo,
        @Size(max = 4000) String descricao,
        @NotBlank @Size(max = 40) String tipoWidget,
        @NotNull @PositiveOrZero Integer ordem,
        @Size(max = 150) String queryReferencia,
        Boolean ativo) {
}
