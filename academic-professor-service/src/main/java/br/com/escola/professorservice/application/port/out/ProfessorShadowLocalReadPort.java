package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorShadowLocalReadPort {

    boolean supportsListarProfessores(InternalRequestContext context);

    List<ProfessorResumoResponse> listarProfessores(InternalRequestContext context);

    Optional<ProfessorResumoResponse> buscarProfessorPorId(InternalRequestContext context, UUID professorId);

    boolean supportsListarAlocacoes(InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId);

    boolean supportsListarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);

    List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);
}
