package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardSecretariaResponse;

public interface DashboardSecretariaPort {

    DashboardSecretariaResponse consultar(String authorization, InternalRequestContext context);
}
