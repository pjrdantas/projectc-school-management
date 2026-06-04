package br.com.escola.dashboard.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardIndicadorSnapshotResponse(
        UUID id,
        UUID publicoDashboardId,
        String publicoCodigo,
        String codigoIndicador,
        String descricao,
        BigDecimal valorNumeric,
        String valorTexto,
        LocalDate referenciaData) {
}
