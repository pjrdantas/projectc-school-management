package br.com.escola.pedagogicalservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarReadUseCase;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarReadPort;

@Service
public class HistoricoEscolarReadService implements HistoricoEscolarReadUseCase {

    private final HistoricoEscolarReadPort historicoEscolarReadPort;

    public HistoricoEscolarReadService(HistoricoEscolarReadPort historicoEscolarReadPort) {
        this.historicoEscolarReadPort = historicoEscolarReadPort;
    }

    @Override
    public String listar(String authorization, InternalRequestContext context, int page, int size) {
        return historicoEscolarReadPort.listar(authorization, context, page, size);
    }

    @Override
    public String listarPorAluno(String authorization, InternalRequestContext context, UUID alunoId) {
        return historicoEscolarReadPort.listarPorAluno(authorization, context, alunoId);
    }

    @Override
    public HistoricoEscolarTelaResponse carregarNovo(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID matriculaId,
            String modo) {
        return historicoEscolarReadPort.carregarNovo(authorization, context, alunoId, matriculaId, modo);
    }

    @Override
    public HistoricoEscolarTelaResponse carregarParaEdicao(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId) {
        return historicoEscolarReadPort.carregarParaEdicao(authorization, context, historicoEscolarId);
    }
}
