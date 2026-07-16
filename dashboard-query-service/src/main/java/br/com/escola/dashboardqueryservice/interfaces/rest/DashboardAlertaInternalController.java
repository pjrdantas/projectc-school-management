package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;
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
import br.com.escola.dashboardqueryservice.application.dto.DashboardAlertaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardAlertaUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardAlertaInternalController {

    private final DashboardAlertaUseCase dashboardAlertaUseCase;

    public DashboardAlertaInternalController(DashboardAlertaUseCase dashboardAlertaUseCase) {
        this.dashboardAlertaUseCase = dashboardAlertaUseCase;
    }

    @GetMapping("/dashboard/alertas")
    public List<DashboardAlertaResponse> consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam @NonNull String publicoCodigo,
            @RequestParam(required = false) UUID professorId) {
        return dashboardAlertaUseCase.consultar(authorization, context, publicoCodigo, professorId);
    }
}
