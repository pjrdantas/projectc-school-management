package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAlertaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardAlertaUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardAlertaPort;

@Service
public class DashboardAlertaService implements DashboardAlertaUseCase {

    private final DashboardAlertaPort dashboardAlertaPort;

    public DashboardAlertaService(DashboardAlertaPort dashboardAlertaPort) {
        this.dashboardAlertaPort = dashboardAlertaPort;
    }

    @Override
    public List<DashboardAlertaResponse> consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID professorId) {
        return dashboardAlertaPort.consultar(authorization, context, publicoCodigo, professorId);
    }
}
