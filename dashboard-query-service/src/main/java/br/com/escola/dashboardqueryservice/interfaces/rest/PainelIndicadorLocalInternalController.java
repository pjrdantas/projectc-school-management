package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.time.LocalDate;
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
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorLocalUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/dashboard/snapshots/locais", "/internal/dashboard/snapshots/locais" })
public class PainelIndicadorLocalInternalController {

    private final PainelIndicadorLocalUseCase useCase;

    public PainelIndicadorLocalInternalController(PainelIndicadorLocalUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<PainelIndicadorSnapshotResponse> listar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam UUID publicoId,
            @RequestParam(required = false) LocalDate referenciaData) {
        return useCase.listar(context, publicoId, referenciaData);
    }

    @PutMapping
    public PainelIndicadorSnapshotResponse salvar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelIndicadorSnapshotCommand command) {
        return useCase.salvar(context, command);
    }

    @DeleteMapping("/{snapshotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID snapshotId) {
        useCase.excluir(context, snapshotId);
    }

    @GetMapping("/historico/publicos/{publicoCodigo}")
    public List<PainelIndicadorHistoricoResponse> historico(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) String codigoIndicador,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        return useCase.historico(context, publicoCodigo, codigoIndicador, dataInicio, dataFim);
    }
}
