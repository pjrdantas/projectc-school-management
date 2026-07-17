package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProfessorResponse;

public interface PainelProfessorUseCase {

    PainelProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId);
}

