package br.com.escola.pedagogicalservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;

public interface AulaUseCase {

    AulaResponse criar(
            String authorization,
            InternalRequestContext context,
            String requestBody);

    List<AulaResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId);

    AulaResponse buscarPorId(
            String authorization,
            InternalRequestContext context,
            UUID aulaId);
}
