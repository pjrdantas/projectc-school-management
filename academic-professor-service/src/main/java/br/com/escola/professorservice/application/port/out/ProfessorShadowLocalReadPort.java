package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;

public interface ProfessorShadowLocalReadPort {

    boolean supportsListarAlocacoes(InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId);

    boolean supportsListarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);

    List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);
}
