package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAcademicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardAcademicoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardAcademicoPort;

@Service
public class DashboardAcademicoService implements DashboardAcademicoUseCase {

    private final DashboardAcademicoPort dashboardAcademicoPort;

    public DashboardAcademicoService(DashboardAcademicoPort dashboardAcademicoPort) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
    }

    @Override
    public DashboardAcademicoResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardAcademicoPort.consultar(authorization, context);
    }
}
