package br.com.escola.dashboardqueryservice.domain.model;

import java.util.UUID;

public record PainelUsuarioPreferencia(
        UUID id,
        UUID escolaId,
        UUID usuarioId,
        UUID widgetId,
        boolean visivel,
        Integer ordem,
        String configuracaoJson) {
}
