package br.com.escola.pedagogicalservice.application.port.out;

import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;

public interface HistoricoEscolarReadPort {

    String listar(String authorization, InternalRequestContext context, int page, int size);

    String listarPorAluno(String authorization, InternalRequestContext context, UUID alunoId);

    HistoricoEscolarTelaResponse carregarNovo(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID matriculaId,
            String modo);

    HistoricoEscolarTelaResponse carregarParaEdicao(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId);
}
