package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardSecretariaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardSecretariaUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardSecretariaPort;

@Service
public class DashboardSecretariaService implements DashboardSecretariaUseCase {

    private final DashboardSecretariaPort dashboardSecretariaPort;

    public DashboardSecretariaService(DashboardSecretariaPort dashboardSecretariaPort) {
        this.dashboardSecretariaPort = dashboardSecretariaPort;
    }

    @Override
    public DashboardSecretariaResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardSecretariaPort.consultar(authorization, context);
    }
}
