package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.dto.NotaAlunoResponse;
import br.com.escola.pedagogicalservice.application.port.out.AvaliacaoPort;

@Repository
public class AvaliacaoAdapter implements AvaliacaoPort {

    private final PersistenciaLocalAdapter persistenciaLocalAdapter;

    public AvaliacaoAdapter(PersistenciaLocalAdapter persistenciaLocalAdapter) {
        this.persistenciaLocalAdapter = persistenciaLocalAdapter;
    }

    @Override
    public AvaliacaoResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        return persistenciaLocalAdapter.criarAvaliacao(authorization, context, requestBody);
    }

    @Override
    public List<AvaliacaoResponse> listar(String authorization, InternalRequestContext context, UUID professorTurmaDisciplinaId, UUID turmaId) {
        return persistenciaLocalAdapter.listarAvaliacoes(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @Override
    public AvaliacaoResponse buscarPorId(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        return persistenciaLocalAdapter.buscarAvaliacaoPorId(authorization, context, avaliacaoId);
    }

    @Override
    public NotaAlunoResponse lancarNota(String authorization, InternalRequestContext context, UUID avaliacaoId, String requestBody) {
        return persistenciaLocalAdapter.lancarNota(authorization, context, avaliacaoId, requestBody);
    }

    @Override
    public List<NotaAlunoResponse> listarNotasPorAvaliacao(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        return persistenciaLocalAdapter.listarNotasPorAvaliacao(authorization, context, avaliacaoId);
    }

    @Override
    public List<NotaAlunoResponse> listarNotasPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return persistenciaLocalAdapter.listarNotasPorMatricula(authorization, context, matriculaId);
    }
}
