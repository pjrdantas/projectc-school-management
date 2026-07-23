package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorGeracaoCompatibilidadeUseCase;

@RestController
@RequestMapping({ "/internal/v1/dashboard/snapshots/geracoes", "/internal/dashboard/snapshots/geracoes" })
public class PainelIndicadorGeracaoCompatibilidadeInternalController {

    private final PainelIndicadorGeracaoCompatibilidadeUseCase useCase;

    public PainelIndicadorGeracaoCompatibilidadeInternalController(
            PainelIndicadorGeracaoCompatibilidadeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/professores/{professorId}")
    public List<PainelIndicadorSnapshotResponse> consultarProfessor(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable UUID professorId,
            @RequestParam(required = false) LocalDate referenciaData) {
        return useCase.consultarPorProfessor(context, professorId, referenciaData);
    }

    @PostMapping("/{publicoCodigo}")
    public List<PainelIndicadorSnapshotResponse> consultarPublico(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return useCase.consultarPorPublico(context, publicoCodigo, referenciaData);
    }
}
