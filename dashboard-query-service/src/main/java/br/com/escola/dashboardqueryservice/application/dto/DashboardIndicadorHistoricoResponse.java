package br.com.escola.dashboardqueryservice.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardIndicadorHistoricoResponse(
        String publicoCodigo,
        String codigoIndicador,
        String descricao,
        BigDecimal valorAtual,
        BigDecimal valorAnterior,
        BigDecimal variacaoPercentual,
        List<DashboardIndicadorHistoricoPontoResponse> pontos) {
}
