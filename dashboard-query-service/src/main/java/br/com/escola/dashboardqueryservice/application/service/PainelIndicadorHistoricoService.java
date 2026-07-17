package br.com.escola.dashboardqueryservice.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorHistoricoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorHistoricoPort;

@Service
public class PainelIndicadorHistoricoService implements PainelIndicadorHistoricoUseCase {

    private final PainelIndicadorHistoricoPort dashboardIndicadorHistoricoPort;

    public PainelIndicadorHistoricoService(PainelIndicadorHistoricoPort dashboardIndicadorHistoricoPort) {
        this.dashboardIndicadorHistoricoPort = dashboardIndicadorHistoricoPort;
    }

    @Override
    public List<PainelIndicadorHistoricoResponse> consultarHistorico(
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

