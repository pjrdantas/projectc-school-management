package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record DashboardPublicoResponse(
        UUID id,
        String codigo,
        String descricao) {
}
