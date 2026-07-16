package br.com.escola.dashboardqueryservice.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorHistoricoResponse;

public interface DashboardIndicadorHistoricoUseCase {

    List<DashboardIndicadorHistoricoResponse> consultarHistorico(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId);
}
