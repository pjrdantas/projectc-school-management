package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardPublicoResponse;

public interface DashboardPublicoPort {

    List<DashboardPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context);
}
