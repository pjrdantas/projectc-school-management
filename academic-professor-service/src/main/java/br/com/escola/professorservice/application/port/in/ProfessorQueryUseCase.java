package br.com.escola.professorservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorQueryUseCase {

    List<ProfessorResumoResponse> listarProfessores(String authorization, InternalRequestContext context);

    ProfessorResumoResponse buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarAlocacoes(String authorization, InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(String authorization, InternalRequestContext context, UUID turmaId);

    List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(String authorization, InternalRequestContext context);
}
