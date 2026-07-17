package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelSecretariaResponse;

public interface PainelSecretariaPort {

    PainelSecretariaResponse consultar(String authorization, InternalRequestContext context);
}

