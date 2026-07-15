package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardDiretorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardDiretorUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardDiretorPort;

@Service
public class DashboardDiretorService implements DashboardDiretorUseCase {

    private final DashboardDiretorPort dashboardDiretorPort;

    public DashboardDiretorService(DashboardDiretorPort dashboardDiretorPort) {
        this.dashboardDiretorPort = dashboardDiretorPort;
    }

    @Override
    public DashboardDiretorResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardDiretorPort.consultar(authorization, context);
    }
}
