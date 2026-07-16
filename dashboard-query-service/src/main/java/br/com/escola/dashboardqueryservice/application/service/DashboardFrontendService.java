package br.com.escola.dashboardqueryservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardFrontendResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardFrontendUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardFrontendPort;

@Service
public class DashboardFrontendService implements DashboardFrontendUseCase {

    private final DashboardFrontendPort dashboardFrontendPort;

    public DashboardFrontendService(DashboardFrontendPort dashboardFrontendPort) {
        this.dashboardFrontendPort = dashboardFrontendPort;
    }

    @Override
    public DashboardFrontendResponse consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId) {
        return dashboardFrontendPort.consultar(authorization, context, publicoCodigo, usuarioId, professorId);
    }
}
