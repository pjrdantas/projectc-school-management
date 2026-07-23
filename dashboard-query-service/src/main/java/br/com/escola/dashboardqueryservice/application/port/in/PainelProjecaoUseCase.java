package br.com.escola.dashboardqueryservice.application.port.in;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;

public interface PainelProjecaoUseCase {

    void atualizar(InternalRequestContext context, PainelProjecaoUpsertRequest request);
}
