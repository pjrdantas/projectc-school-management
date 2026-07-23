package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAlertaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAlertaUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAlertaPort;

@Service
public class PainelAlertaService implements PainelAlertaUseCase {

    private final PainelAlertaPort dashboardAlertaPort;

    public PainelAlertaService(PainelAlertaPort dashboardAlertaPort) {
        this.dashboardAlertaPort = dashboardAlertaPort;
    }

    @Override
    public List<PainelAlertaResponse> consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID professorId) {
        return dashboardAlertaPort.consultar(authorization, context, publicoCodigo, professorId);
    }
}

