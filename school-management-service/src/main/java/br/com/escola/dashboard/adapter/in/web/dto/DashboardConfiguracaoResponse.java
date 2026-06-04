package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardConfiguracaoResponse(
        UUID id,
        UUID publicoDashboardId,
        String publicoCodigo,
        String codigo,
        String nome,
        String descricao,
        Boolean ativo) {
}
