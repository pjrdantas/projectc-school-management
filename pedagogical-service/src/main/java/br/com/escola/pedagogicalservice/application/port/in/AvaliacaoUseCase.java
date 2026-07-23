package br.com.escola.pedagogicalservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.dto.NotaAlunoResponse;

public interface AvaliacaoUseCase {

    AvaliacaoResponse criar(
            String authorization,
            InternalRequestContext context,
            String requestBody);

    List<AvaliacaoResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId);

    AvaliacaoResponse buscarPorId(
            String authorization,
            InternalRequestContext context,
            UUID avaliacaoId);

    NotaAlunoResponse lancarNota(
            String authorization,
            InternalRequestContext context,
            UUID avaliacaoId,
            String requestBody);

    List<NotaAlunoResponse> listarNotasPorAvaliacao(
            String authorization,
            InternalRequestContext context,
            UUID avaliacaoId);

    List<NotaAlunoResponse> listarNotasPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId);
}
