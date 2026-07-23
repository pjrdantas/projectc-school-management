package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarReadPort;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarWritePort;

@Repository
public class HistoricoEscolarAdapter implements HistoricoEscolarReadPort, HistoricoEscolarWritePort {

    private final PersistenciaLocalAdapter persistenciaLocalAdapter;

    public HistoricoEscolarAdapter(PersistenciaLocalAdapter persistenciaLocalAdapter) {
        this.persistenciaLocalAdapter = persistenciaLocalAdapter;
    }

    @Override
    public HistoricoEscolarTelaResponse carregarNovo(String authorization, InternalRequestContext context, UUID alunoId, UUID matriculaId, String modo) {
        return persistenciaLocalAdapter.carregarNovo(authorization, context, alunoId, matriculaId, modo);
    }

    @Override public String listar(String authorization, InternalRequestContext context, int page, int size) { return persistenciaLocalAdapter.listarHistoricosEscolares(context, page, size); }

    @Override public String listarPorAluno(String authorization, InternalRequestContext context, UUID alunoId) { return persistenciaLocalAdapter.listarHistoricosEscolaresPorAluno(context, alunoId); }

    @Override
    public HistoricoEscolarTelaResponse carregarParaEdicao(String authorization, InternalRequestContext context, UUID historicoEscolarId) {
        return persistenciaLocalAdapter.carregarHistoricoParaEdicao(authorization, context, historicoEscolarId);
    }

    @Override
    public ResponseEntity<String> criar(String authorization, InternalRequestContext context, String requestBody) {
        return persistenciaLocalAdapter.criarHistoricoEscolar(authorization, context, requestBody);
    }

    @Override
    public ResponseEntity<String> atualizar(String authorization, InternalRequestContext context, UUID historicoEscolarId, String requestBody) {
        return persistenciaLocalAdapter.atualizarHistoricoEscolar(authorization, context, historicoEscolarId, requestBody);
    }

    @Override public void excluir(InternalRequestContext context, UUID historicoEscolarId) { persistenciaLocalAdapter.excluirHistoricoEscolar(context, historicoEscolarId); }
}
