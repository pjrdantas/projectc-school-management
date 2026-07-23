package br.com.escola.dashboardqueryservice.application.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PainelUsuarioPreferenciaCommand(
        Boolean visivel,
        @PositiveOrZero Integer ordem,
        @Size(max = 4000) String configuracaoJson) {
}
