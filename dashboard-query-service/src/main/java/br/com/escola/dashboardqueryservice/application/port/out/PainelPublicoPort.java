package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;

public interface PainelPublicoPort {

    List<PainelPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context);
}

