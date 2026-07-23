package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAcademicoResponse;

public interface PainelAcademicoPort {

    PainelAcademicoResponse consultarAcademico(String authorization, InternalRequestContext context);
}

