package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardConfiguracaoUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardConfiguracaoInternalController {

    private final DashboardConfiguracaoUseCase dashboardConfiguracaoUseCase;

    public DashboardConfiguracaoInternalController(DashboardConfiguracaoUseCase dashboardConfiguracaoUseCase) {
        this.dashboardConfiguracaoUseCase = dashboardConfiguracaoUseCase;
    }

    @GetMapping("/dashboard/configuracoes/dashboards")
    public List<DashboardConfiguracaoResponse> listarDashboards(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) UUID publicoDashboardId,
            @RequestParam(required = false) String publicoCodigo) {
        return dashboardConfiguracaoUseCase.listarDashboards(authorization, context, publicoDashboardId, publicoCodigo);
    }
}
