package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardPublicoResponse;

public interface DashboardPublicoUseCase {

    List<DashboardPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context);
}
