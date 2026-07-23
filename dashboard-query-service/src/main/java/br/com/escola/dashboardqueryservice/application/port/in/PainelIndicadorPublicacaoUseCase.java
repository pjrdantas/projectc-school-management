package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorPublicacaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;

public interface PainelIndicadorPublicacaoUseCase {

    List<PainelIndicadorSnapshotResponse> publicar(
            InternalRequestContext context, PainelIndicadorPublicacaoCommand command);
}
