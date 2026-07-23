package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProfessorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelProfessorUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelProfessorInternalController {

    private final PainelProfessorUseCase dashboardProfessorUseCase;

    public PainelProfessorInternalController(PainelProfessorUseCase dashboardProfessorUseCase) {
        this.dashboardProfessorUseCase = dashboardProfessorUseCase;
    }

    @GetMapping("/dashboard/professores/{professorId}")
    public PainelProfessorResponse consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID professorId) {
        return dashboardProfessorUseCase.consultar(authorization, context, professorId);
    }
}

