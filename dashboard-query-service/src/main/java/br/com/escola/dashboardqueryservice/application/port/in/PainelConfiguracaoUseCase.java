package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;

public interface PainelConfiguracaoUseCase {

    List<PainelConfiguracaoResponse> listarPainels(
            String authorization,
            InternalRequestContext context,
            UUID publicoPainelId,
            String publicoCodigo);
}

