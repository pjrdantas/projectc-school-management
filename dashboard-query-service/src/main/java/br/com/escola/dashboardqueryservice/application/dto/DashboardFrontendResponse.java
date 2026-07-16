package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record DashboardFrontendResponse(
        String publicoCodigo,
        UUID usuarioId,
        UUID professorId,
        Object resumo,
        List<DashboardAlertaResponse> alertas,
        List<DashboardFrontendConfiguracaoResponse> dashboards,
        List<DashboardUsuarioConfiguracaoResponse> configuracoesUsuario,
        List<DashboardIndicadorHistoricoResponse> historico) {
}
