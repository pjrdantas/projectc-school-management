package br.com.escola.dashboardqueryservice.interfaces.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelDiretorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelDiretorUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelDiretorInternalController {

    private final PainelDiretorUseCase dashboardDiretorUseCase;

    public PainelDiretorInternalController(PainelDiretorUseCase dashboardDiretorUseCase) {
        this.dashboardDiretorUseCase = dashboardDiretorUseCase;
    }

    @GetMapping("/dashboard/diretor")
    public PainelDiretorResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardDiretorUseCase.consultar(authorization, context);
    }
}

