package br.com.escola.dashboardqueryservice.domain.model;

import java.util.UUID;

public record PainelConfiguracao(
        UUID id,
        UUID escolaId,
        UUID publicoId,
        String codigo,
        String nome,
        String descricao,
        boolean ativo) {
}
