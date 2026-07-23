package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record PainelUsuarioPreferenciaResponse(
        UUID id,
        UUID usuarioId,
        UUID widgetId,
        String widgetCodigo,
        String widgetTitulo,
        UUID painelId,
        String painelCodigo,
        Boolean visivel,
        Integer ordem,
        String configuracaoJson) {
}
