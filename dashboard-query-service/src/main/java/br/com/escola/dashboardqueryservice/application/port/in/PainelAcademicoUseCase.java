package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAcademicoResponse;

public interface PainelAcademicoUseCase {

    PainelAcademicoResponse consultar(String authorization, InternalRequestContext context);
}

