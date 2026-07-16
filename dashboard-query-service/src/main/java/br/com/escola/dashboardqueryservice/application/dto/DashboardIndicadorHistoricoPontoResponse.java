package br.com.escola.dashboardqueryservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardIndicadorHistoricoPontoResponse(
        LocalDate referenciaData,
        BigDecimal valorNumeric,
        String valorTexto) {
}
