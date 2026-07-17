package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelSecretariaResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelSecretariaUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelSecretariaPort;

@Service
public class PainelSecretariaService implements PainelSecretariaUseCase {

    private final PainelSecretariaPort dashboardSecretariaPort;

    public PainelSecretariaService(PainelSecretariaPort dashboardSecretariaPort) {
        this.dashboardSecretariaPort = dashboardSecretariaPort;
    }

    @Override
    public PainelSecretariaResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardSecretariaPort.consultar(authorization, context);
    }
}

