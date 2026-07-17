package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelPublicoUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelPublicoInternalController {

    private final PainelPublicoUseCase dashboardPublicoUseCase;

    public PainelPublicoInternalController(PainelPublicoUseCase dashboardPublicoUseCase) {
        this.dashboardPublicoUseCase = dashboardPublicoUseCase;
    }

    @GetMapping("/dashboard/configuracoes/publicos")
    public List<PainelPublicoResponse> listarPublicos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardPublicoUseCase.listarPublicos(authorization, context);
    }
}

