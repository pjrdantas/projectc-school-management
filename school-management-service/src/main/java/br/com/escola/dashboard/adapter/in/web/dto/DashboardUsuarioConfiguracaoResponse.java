package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardUsuarioConfiguracaoResponse(
        UUID id,
        UUID usuarioId,
        UUID dashboardWidgetId,
        String widgetCodigo,
        String widgetTitulo,
        UUID dashboardId,
        String dashboardCodigo,
        Boolean visivel,
        Integer ordem,
        String configuracaoJson) {
}
