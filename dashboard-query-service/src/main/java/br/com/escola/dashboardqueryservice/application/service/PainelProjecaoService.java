package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;
import br.com.escola.dashboardqueryservice.application.port.in.PainelProjecaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelProjecaoPort;

@Service
public class PainelProjecaoService implements PainelProjecaoUseCase {

    private final PainelProjecaoPort painelProjecaoPort;

    public PainelProjecaoService(PainelProjecaoPort painelProjecaoPort) {
        this.painelProjecaoPort = painelProjecaoPort;
    }

    @Override
    public void atualizar(InternalRequestContext context, PainelProjecaoUpsertRequest request) {
        painelProjecaoPort.salvar(context.escolaId(), request);
    }
}
