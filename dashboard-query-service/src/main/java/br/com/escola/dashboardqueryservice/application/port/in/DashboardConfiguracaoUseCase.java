package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardConfiguracaoResponse;

public interface DashboardConfiguracaoUseCase {

    List<DashboardConfiguracaoResponse> listarDashboards(
            String authorization,
            InternalRequestContext context,
            UUID publicoDashboardId,
            String publicoCodigo);
}
