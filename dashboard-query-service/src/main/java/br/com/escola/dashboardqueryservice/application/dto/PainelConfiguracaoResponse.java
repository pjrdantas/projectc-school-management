package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record PainelConfiguracaoResponse(
        UUID id,
        UUID publicoPainelId,
        String publicoCodigo,
        String codigo,
        String nome,
        String descricao,
        Boolean ativo) {
}

