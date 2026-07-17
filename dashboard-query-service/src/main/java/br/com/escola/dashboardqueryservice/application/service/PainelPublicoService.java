package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelPublicoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelPublicoPort;

@Service
public class PainelPublicoService implements PainelPublicoUseCase {

    private final PainelPublicoPort dashboardPublicoPort;

    public PainelPublicoService(PainelPublicoPort dashboardPublicoPort) {
        this.dashboardPublicoPort = dashboardPublicoPort;
    }

    @Override
    public List<PainelPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context) {
        return dashboardPublicoPort.listarPublicos(authorization, context);
    }
}

