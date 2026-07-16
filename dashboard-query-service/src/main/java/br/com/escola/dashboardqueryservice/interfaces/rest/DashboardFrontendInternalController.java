package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardFrontendResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardFrontendUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardFrontendInternalController {

    private final DashboardFrontendUseCase dashboardFrontendUseCase;

    public DashboardFrontendInternalController(DashboardFrontendUseCase dashboardFrontendUseCase) {
        this.dashboardFrontendUseCase = dashboardFrontendUseCase;
    }

    @GetMapping("/dashboard/frontend")
    public DashboardFrontendResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam @NonNull String publicoCodigo,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) UUID professorId) {
        return dashboardFrontendUseCase.consultar(authorization, context, publicoCodigo, usuarioId, professorId);
    }
}
