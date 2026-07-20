package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelDiretorResponse;

public interface PainelDiretorPort {

    PainelDiretorResponse consultarDiretor(String authorization, InternalRequestContext context);
}

