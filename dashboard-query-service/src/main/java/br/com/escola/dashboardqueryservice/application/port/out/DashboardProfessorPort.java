package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardProfessorResponse;

public interface DashboardProfessorPort {

    DashboardProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId);
}
