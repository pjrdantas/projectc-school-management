package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface ConsultaPort {

    List<ResumoResponse> listarProfessores(String authorization, InternalRequestContext context);

    Optional<ResumoResponse> buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId);

    List<AlocacaoResponse> listarAlocacoes(String authorization, InternalRequestContext context, UUID professorId);

    List<AlocacaoResponse> listarProfessoresPorTurma(String authorization, InternalRequestContext context, UUID turmaId);

    List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(String authorization, InternalRequestContext context);
}

