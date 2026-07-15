package br.com.escola.dashboardqueryservice.interfaces.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardDiretorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardDiretorUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardDiretorInternalController {

    private final DashboardDiretorUseCase dashboardDiretorUseCase;

    public DashboardDiretorInternalController(DashboardDiretorUseCase dashboardDiretorUseCase) {
        this.dashboardDiretorUseCase = dashboardDiretorUseCase;
    }

    @GetMapping("/dashboard/diretor")
    public DashboardDiretorResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardDiretorUseCase.consultar(authorization, context);
    }
}
