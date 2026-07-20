package br.com.escola.dashboardqueryservice.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;
import br.com.escola.dashboardqueryservice.application.port.in.PainelProjecaoUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/dashboard/projecoes", "/internal/dashboard/projecoes" })
public class PainelProjecaoInternalController {

    private final PainelProjecaoUseCase painelProjecaoUseCase;

    public PainelProjecaoInternalController(PainelProjecaoUseCase painelProjecaoUseCase) {
        this.painelProjecaoUseCase = painelProjecaoUseCase;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelProjecaoUpsertRequest request) {
        painelProjecaoUseCase.atualizar(context, request);
    }
}
