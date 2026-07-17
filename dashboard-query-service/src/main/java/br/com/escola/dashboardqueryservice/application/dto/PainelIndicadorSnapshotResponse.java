package br.com.escola.dashboardqueryservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PainelIndicadorSnapshotResponse(
        UUID id,
        UUID publicoPainelId,
        String publicoCodigo,
        UUID escolaId,
        String escolaNome,
        String codigoIndicador,
        String descricao,
        BigDecimal valorNumeric,
        String valorTexto,
        LocalDate referenciaData) {
}

