package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardWidgetResponse(
        UUID id,
        UUID dashboardId,
        String dashboardCodigo,
        String codigo,
        String titulo,
        String descricao,
        String tipoWidget,
        Integer ordem,
        String queryReferencia,
        Boolean ativo) {
}
