package br.com.escola.dashboardqueryservice.interfaces.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardSecretariaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardSecretariaUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardSecretariaInternalController {

    private final DashboardSecretariaUseCase dashboardSecretariaUseCase;

    public DashboardSecretariaInternalController(DashboardSecretariaUseCase dashboardSecretariaUseCase) {
        this.dashboardSecretariaUseCase = dashboardSecretariaUseCase;
    }

    @GetMapping("/dashboard/secretaria")
    public DashboardSecretariaResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardSecretariaUseCase.consultar(authorization, context);
    }
}
