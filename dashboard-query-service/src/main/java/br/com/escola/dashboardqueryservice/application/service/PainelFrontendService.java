package br.com.escola.dashboardqueryservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelFrontendResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelFrontendUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelFrontendPort;

@Service
public class PainelFrontendService implements PainelFrontendUseCase {

    private final PainelFrontendPort dashboardFrontendPort;

    public PainelFrontendService(PainelFrontendPort dashboardFrontendPort) {
        this.dashboardFrontendPort = dashboardFrontendPort;
    }

    @Override
    public PainelFrontendResponse consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId) {
        return dashboardFrontendPort.consultar(authorization, context, publicoCodigo, usuarioId, professorId);
    }
}

