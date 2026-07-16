package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardConfiguracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardConfiguracaoPort;

@Service
public class DashboardConfiguracaoService implements DashboardConfiguracaoUseCase {

    private final DashboardConfiguracaoPort dashboardConfiguracaoPort;

    public DashboardConfiguracaoService(DashboardConfiguracaoPort dashboardConfiguracaoPort) {
        this.dashboardConfiguracaoPort = dashboardConfiguracaoPort;
    }

    @Override
    public List<DashboardConfiguracaoResponse> listarDashboards(
            String authorization,
            InternalRequestContext context,
            UUID publicoDashboardId,
            String publicoCodigo) {
        return dashboardConfiguracaoPort.listarDashboards(authorization, context, publicoDashboardId, publicoCodigo);
    }
}
