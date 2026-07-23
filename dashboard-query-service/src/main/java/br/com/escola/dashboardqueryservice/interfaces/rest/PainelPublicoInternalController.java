package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAdministracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelPublicoUseCase;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelPublicoInternalController {

    private final PainelPublicoUseCase dashboardPublicoUseCase;
    private final PainelAdministracaoUseCase administracaoUseCase;

    public PainelPublicoInternalController(
            PainelPublicoUseCase dashboardPublicoUseCase,
            PainelAdministracaoUseCase administracaoUseCase) {
        this.dashboardPublicoUseCase = dashboardPublicoUseCase;
        this.administracaoUseCase = administracaoUseCase;
    }

    @GetMapping("/dashboard/configuracoes/publicos")
    public List<PainelPublicoResponse> listarPublicos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return dashboardPublicoUseCase.listarPublicos(authorization, context);
    }

    @PostMapping("/dashboard/configuracoes/publicos")
    @ResponseStatus(HttpStatus.CREATED)
    public PainelPublico criarPublico(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelPublicoCommand command) {
        return administracaoUseCase.criarPublico(context, command);
    }

    @PutMapping("/dashboard/configuracoes/publicos/{publicoId}")
    public PainelPublico atualizarPublico(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID publicoId,
            @Valid @RequestBody PainelPublicoCommand command) {
        return administracaoUseCase.atualizarPublico(context, publicoId, command);
    }

    @DeleteMapping("/dashboard/configuracoes/publicos/{publicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirPublico(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID publicoId) {
        administracaoUseCase.excluirPublico(context, publicoId);
    }
}

