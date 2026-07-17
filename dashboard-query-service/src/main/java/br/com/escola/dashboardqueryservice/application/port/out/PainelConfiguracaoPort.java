package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;

public interface PainelConfiguracaoPort {

    List<PainelConfiguracaoResponse> listarPainels(
            String authorization,
            InternalRequestContext context,
            UUID publicoPainelId,
            String publicoCodigo);
}

