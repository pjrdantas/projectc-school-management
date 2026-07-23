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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelWidgetCommand;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAdministracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelConfiguracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelWidgetUseCase;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelConfiguracaoInternalController {

    private final PainelConfiguracaoUseCase dashboardConfiguracaoUseCase;
    private final PainelAdministracaoUseCase administracaoUseCase;
    private final PainelWidgetUseCase widgetUseCase;

    public PainelConfiguracaoInternalController(
            PainelConfiguracaoUseCase dashboardConfiguracaoUseCase,
            PainelAdministracaoUseCase administracaoUseCase,
            PainelWidgetUseCase widgetUseCase) {
        this.dashboardConfiguracaoUseCase = dashboardConfiguracaoUseCase;
        this.administracaoUseCase = administracaoUseCase;
        this.widgetUseCase = widgetUseCase;
    }

    @GetMapping("/dashboard/configuracoes/dashboards")
    public List<PainelConfiguracaoResponse> listarPainels(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) UUID publicoPainelId,
            @RequestParam(required = false) String publicoCodigo) {
        return dashboardConfiguracaoUseCase.listarPainels(authorization, context, publicoPainelId, publicoCodigo);
    }

    @PostMapping("/dashboard/configuracoes/dashboards")
    @ResponseStatus(HttpStatus.CREATED)
    public PainelConfiguracao criarPainel(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelConfiguracaoCommand command) {
        return administracaoUseCase.criarPainel(context, command);
    }

    @PutMapping("/dashboard/configuracoes/dashboards/{painelId}")
    public PainelConfiguracao atualizarPainel(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID painelId,
            @Valid @RequestBody PainelConfiguracaoCommand command) {
        return administracaoUseCase.atualizarPainel(context, painelId, command);
    }

    @DeleteMapping("/dashboard/configuracoes/dashboards/{painelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirPainel(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID painelId) {
        administracaoUseCase.excluirPainel(context, painelId);
    }

    @GetMapping("/dashboard/configuracoes/dashboards/{painelId}/widgets")
    public List<PainelWidget> listarWidgets(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID painelId) {
        return widgetUseCase.listarWidgets(context, painelId);
    }

    @PostMapping("/dashboard/configuracoes/widgets")
    @ResponseStatus(HttpStatus.CREATED)
    public PainelWidget criarWidget(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelWidgetCommand command) {
        return widgetUseCase.criarWidget(context, command);
    }

    @PutMapping("/dashboard/configuracoes/widgets/{widgetId}")
    public PainelWidget atualizarWidget(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID widgetId,
            @Valid @RequestBody PainelWidgetCommand command) {
        return widgetUseCase.atualizarWidget(context, widgetId, command);
    }

    @DeleteMapping("/dashboard/configuracoes/widgets/{widgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirWidget(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID widgetId) {
        widgetUseCase.excluirWidget(context, widgetId);
    }
}

