package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;

public interface PainelPublicoUseCase {

    List<PainelPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context);
}

