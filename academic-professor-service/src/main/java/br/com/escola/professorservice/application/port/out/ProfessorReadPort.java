package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorReadPort {

    List<ProfessorResumoResponse> listarProfessores(String authorization, InternalRequestContext context);

    Optional<ProfessorResumoResponse> buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarAlocacoes(String authorization, InternalRequestContext context, UUID professorId);

    List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(String authorization, InternalRequestContext context, UUID turmaId);

    List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(String authorization, InternalRequestContext context);
}
