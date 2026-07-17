package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorHistoricoUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PainelIndicadorHistoricoInternalController {

    private final PainelIndicadorHistoricoUseCase dashboardIndicadorHistoricoUseCase;

    public PainelIndicadorHistoricoInternalController(PainelIndicadorHistoricoUseCase dashboardIndicadorHistoricoUseCase) {
        this.dashboardIndicadorHistoricoUseCase = dashboardIndicadorHistoricoUseCase;
    }

    @GetMapping("/dashboard/snapshots/historico/publicos/{publicoCodigo}")
    public List<PainelIndicadorHistoricoResponse> consultarHistorico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull String publicoCodigo,
            @RequestParam(required = false) String codigoIndicador,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) UUID professorId) {
        return dashboardIndicadorHistoricoUseCase.consultarHistorico(
                authorization,
                context,
                publicoCodigo,
                codigoIndicador,
                dataInicio,
                dataFim,
                professorId);
    }
}

