package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProfessorResponse;

public interface PainelProfessorPort {

    PainelProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId);
}

