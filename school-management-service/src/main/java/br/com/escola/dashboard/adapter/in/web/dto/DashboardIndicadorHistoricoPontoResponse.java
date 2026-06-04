package br.com.escola.dashboard.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardIndicadorHistoricoPontoResponse(
        LocalDate referenciaData,
        BigDecimal valorNumeric,
        String valorTexto) {
}
