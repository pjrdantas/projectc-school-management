package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardDiretorResponse;

public interface DashboardDiretorUseCase {

    DashboardDiretorResponse consultar(String authorization, InternalRequestContext context);
}
