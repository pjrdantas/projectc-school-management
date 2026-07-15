package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardDiretorResponse;

public interface DashboardDiretorPort {

    DashboardDiretorResponse consultar(String authorization, InternalRequestContext context);
}
