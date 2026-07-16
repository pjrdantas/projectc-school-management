package br.com.escola.dashboardqueryservice.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardIndicadorHistoricoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardIndicadorHistoricoPort;

@Service
public class DashboardIndicadorHistoricoService implements DashboardIndicadorHistoricoUseCase {

    private final DashboardIndicadorHistoricoPort dashboardIndicadorHistoricoPort;

    public DashboardIndicadorHistoricoService(DashboardIndicadorHistoricoPort dashboardIndicadorHistoricoPort) {
        this.dashboardIndicadorHistoricoPort = dashboardIndicadorHistoricoPort;
    }

    @Override
    public List<DashboardIndicadorHistoricoResponse> consultarHistorico(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        return dashboardIndicadorHistoricoPort.consultarHistorico(
                authorization,
                context,
                publicoCodigo,
                codigoIndicador,
                dataInicio,
                dataFim,
                professorId);
    }
}
