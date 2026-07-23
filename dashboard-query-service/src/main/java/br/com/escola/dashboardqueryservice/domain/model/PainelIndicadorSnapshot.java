package br.com.escola.dashboardqueryservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PainelIndicadorSnapshot(
        UUID id,
        UUID escolaId,
        UUID publicoId,
        String codigoIndicador,
        String descricao,
        BigDecimal valorNumeric,
        String valorTexto,
        String escolaNome,
        LocalDate referenciaData) {
}
