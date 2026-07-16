package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardConfiguracaoResponse;

public interface DashboardConfiguracaoPort {

    List<DashboardConfiguracaoResponse> listarDashboards(
            String authorization,
            InternalRequestContext context,
            UUID publicoDashboardId,
            String publicoCodigo);
}
