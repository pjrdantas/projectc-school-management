package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface LeituraLocalPort {

    List<ResumoResponse> listarProfessores(InternalRequestContext context);

    ResumoResponse buscarProfessorPorId(InternalRequestContext context, UUID professorId);

    List<AlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId);

    List<AlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);
}

