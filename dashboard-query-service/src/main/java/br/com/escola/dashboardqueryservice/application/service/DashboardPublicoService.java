package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardPublicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardPublicoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardPublicoPort;

@Service
public class DashboardPublicoService implements DashboardPublicoUseCase {

    private final DashboardPublicoPort dashboardPublicoPort;

    public DashboardPublicoService(DashboardPublicoPort dashboardPublicoPort) {
        this.dashboardPublicoPort = dashboardPublicoPort;
    }

    @Override
    public List<DashboardPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context) {
        return dashboardPublicoPort.listarPublicos(authorization, context);
    }
}
