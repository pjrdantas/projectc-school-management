package br.com.escola.dashboardqueryservice.domain.model;

import java.util.UUID;

public record PainelPublico(UUID id, UUID escolaId, String codigo, String descricao) {
}
