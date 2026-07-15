package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAcademicoResponse;

public interface DashboardAcademicoPort {

    DashboardAcademicoResponse consultar(String authorization, InternalRequestContext context);
}
