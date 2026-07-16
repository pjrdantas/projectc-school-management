package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAlertaResponse;

public interface DashboardAlertaUseCase {

    List<DashboardAlertaResponse> consultar(String authorization, InternalRequestContext context, String publicoCodigo, UUID professorId);
}
