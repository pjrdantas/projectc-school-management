package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record PainelUsuarioConfiguracaoResponse(
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

