package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelSecretariaResponse;

public interface PainelSecretariaUseCase {

    PainelSecretariaResponse consultar(String authorization, InternalRequestContext context);
}

