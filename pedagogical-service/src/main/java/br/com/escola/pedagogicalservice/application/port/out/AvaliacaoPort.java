package br.com.escola.pedagogicalservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;

public interface AvaliacaoPort {

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
}
