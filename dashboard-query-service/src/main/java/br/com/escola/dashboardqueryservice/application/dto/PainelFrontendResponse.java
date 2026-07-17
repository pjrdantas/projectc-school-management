package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record PainelFrontendResponse(
        String publicoCodigo,
        UUID usuarioId,
        UUID professorId,
        Object resumo,
        List<PainelAlertaResponse> alertas,
        List<PainelFrontendConfiguracaoResponse> dashboards,
        List<PainelUsuarioConfiguracaoResponse> configuracoesUsuario,
        List<PainelIndicadorHistoricoResponse> historico) {
}

