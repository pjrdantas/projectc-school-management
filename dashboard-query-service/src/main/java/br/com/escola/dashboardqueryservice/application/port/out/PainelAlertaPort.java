package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAlertaResponse;

public interface PainelAlertaPort {

    List<PainelAlertaResponse> consultar(String authorization, InternalRequestContext context, String publicoCodigo, UUID professorId);
}

