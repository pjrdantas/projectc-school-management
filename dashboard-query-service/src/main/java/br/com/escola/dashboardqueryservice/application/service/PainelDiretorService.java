package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelDiretorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelDiretorUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelDiretorPort;

@Service
public class PainelDiretorService implements PainelDiretorUseCase {

    private final PainelDiretorPort dashboardDiretorPort;

    public PainelDiretorService(PainelDiretorPort dashboardDiretorPort) {
        this.dashboardDiretorPort = dashboardDiretorPort;
    }

    @Override
    public PainelDiretorResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardDiretorPort.consultar(authorization, context);
    }
}

