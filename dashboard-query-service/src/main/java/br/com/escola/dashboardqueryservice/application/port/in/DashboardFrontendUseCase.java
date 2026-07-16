package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardFrontendResponse;

public interface DashboardFrontendUseCase {

    DashboardFrontendResponse consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId);
}
