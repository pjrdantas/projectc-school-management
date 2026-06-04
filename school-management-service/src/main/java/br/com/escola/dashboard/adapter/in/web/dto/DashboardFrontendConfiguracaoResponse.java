package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record DashboardFrontendConfiguracaoResponse(
        UUID id,
        String codigo,
        String nome,
        String descricao,
        Boolean ativo,
        List<DashboardFrontendWidgetResponse> widgets) {
}
