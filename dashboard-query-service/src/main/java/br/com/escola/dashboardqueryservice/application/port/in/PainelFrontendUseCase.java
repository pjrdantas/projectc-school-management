package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelFrontendResponse;

public interface PainelFrontendUseCase {

    PainelFrontendResponse consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId);
}

