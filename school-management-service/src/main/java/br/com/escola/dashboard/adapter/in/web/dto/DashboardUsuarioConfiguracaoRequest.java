package br.com.escola.dashboard.adapter.in.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record DashboardUsuarioConfiguracaoRequest(
        Boolean visivel,
        @PositiveOrZero Integer ordem,
        String configuracaoJson) {
}
