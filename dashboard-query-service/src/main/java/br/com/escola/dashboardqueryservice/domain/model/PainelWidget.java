package br.com.escola.dashboardqueryservice.domain.model;

import java.util.UUID;

public record PainelWidget(
        UUID id,
        UUID escolaId,
        UUID painelId,
        String codigo,
        String titulo,
        String descricao,
        String tipoWidget,
        int ordem,
        String queryReferencia,
        boolean ativo) {
}
