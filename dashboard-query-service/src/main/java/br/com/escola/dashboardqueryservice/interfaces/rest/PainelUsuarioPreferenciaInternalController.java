package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelUsuarioPreferenciaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelUsuarioPreferenciaInternalController {

    private final PainelUsuarioPreferenciaUseCase useCase;

    public PainelUsuarioPreferenciaInternalController(PainelUsuarioPreferenciaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/dashboard/usuarios/{usuarioId}/configuracoes")
    public List<PainelUsuarioPreferenciaResponse> listar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID usuarioId,
            @RequestParam(required = false) UUID painelId) {
        return useCase.listar(context, usuarioId, painelId);
    }

    @PutMapping("/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao")
    public PainelUsuarioPreferenciaResponse salvar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID usuarioId,
            @PathVariable UUID widgetId,
            @Valid @RequestBody PainelUsuarioPreferenciaCommand command) {
        return useCase.salvar(context, usuarioId, widgetId, command);
    }

    @DeleteMapping("/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID usuarioId,
            @PathVariable UUID widgetId) {
        useCase.excluir(context, usuarioId, widgetId);
    }
}
