package br.com.escola.dashboardqueryservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardProfessorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardProfessorUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardProfessorPort;

@Service
public class DashboardProfessorService implements DashboardProfessorUseCase {

    private final DashboardProfessorPort dashboardProfessorPort;

    public DashboardProfessorService(DashboardProfessorPort dashboardProfessorPort) {
        this.dashboardProfessorPort = dashboardProfessorPort;
    }

    @Override
    public DashboardProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId) {
        return dashboardProfessorPort.consultar(authorization, context, professorId);
    }
}
