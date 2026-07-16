package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardProfessorResponse;

public interface DashboardProfessorUseCase {

    DashboardProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId);
}
