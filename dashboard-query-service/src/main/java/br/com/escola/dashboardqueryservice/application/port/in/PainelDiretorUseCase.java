package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelDiretorResponse;

public interface PainelDiretorUseCase {

    PainelDiretorResponse consultar(String authorization, InternalRequestContext context);
}

