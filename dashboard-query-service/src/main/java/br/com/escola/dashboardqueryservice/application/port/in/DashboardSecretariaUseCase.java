package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardSecretariaResponse;

public interface DashboardSecretariaUseCase {

    DashboardSecretariaResponse consultar(String authorization, InternalRequestContext context);
}
