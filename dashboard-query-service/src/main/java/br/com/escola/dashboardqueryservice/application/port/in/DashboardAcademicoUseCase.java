package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAcademicoResponse;

public interface DashboardAcademicoUseCase {

    DashboardAcademicoResponse consultar(String authorization, InternalRequestContext context);
}
