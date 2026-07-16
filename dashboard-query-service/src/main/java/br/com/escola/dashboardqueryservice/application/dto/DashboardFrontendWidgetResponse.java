package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record DashboardFrontendWidgetResponse(
        UUID id,
        String codigo,
        String titulo,
        String descricao,
        String tipoWidget,
        Integer ordem,
        String queryReferencia,
        Boolean ativo) {
}
