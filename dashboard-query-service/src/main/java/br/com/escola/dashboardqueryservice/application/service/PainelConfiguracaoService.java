package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelConfiguracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelConfiguracaoPort;

@Service
public class PainelConfiguracaoService implements PainelConfiguracaoUseCase {

    private final PainelConfiguracaoPort dashboardConfiguracaoPort;

    public PainelConfiguracaoService(PainelConfiguracaoPort dashboardConfiguracaoPort) {
        this.dashboardConfiguracaoPort = dashboardConfiguracaoPort;
    }

    @Override
    public List<PainelConfiguracaoResponse> listarPainels(
            String authorization,
            InternalRequestContext context,
            UUID publicoPainelId,
            String publicoCodigo) {
        return dashboardConfiguracaoPort.listarPainels(authorization, context, publicoPainelId, publicoCodigo);
    }
}

