package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorPublicacaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorPublicacaoUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/dashboard/indicadores/publicacoes", "/internal/dashboard/indicadores/publicacoes" })
public class PainelIndicadorPublicacaoInternalController {

    private final PainelIndicadorPublicacaoUseCase useCase;

    public PainelIndicadorPublicacaoInternalController(PainelIndicadorPublicacaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PutMapping
    public List<PainelIndicadorSnapshotResponse> publicar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody PainelIndicadorPublicacaoCommand command) {
        return useCase.publicar(context, command);
    }
}
