package br.com.escola.dashboardqueryservice.interfaces.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAcademicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAcademicoUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelAcademicoInternalController {

    private final PainelAcademicoUseCase dashboardAcademicoUseCase;

    public PainelAcademicoInternalController(PainelAcademicoUseCase dashboardAcademicoUseCase) {
        this.dashboardAcademicoUseCase = dashboardAcademicoUseCase;
    }

    @GetMapping("/dashboard/academico")
    public PainelAcademicoResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardAcademicoUseCase.consultar(authorization, context);
    }
}

