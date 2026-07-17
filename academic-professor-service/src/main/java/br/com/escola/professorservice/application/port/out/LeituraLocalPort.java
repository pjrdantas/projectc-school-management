package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface LeituraLocalPort {

    boolean supportsListarProfessores(InternalRequestContext context);

    List<ResumoResponse> listarProfessores(InternalRequestContext context);

    boolean supportsBuscarProfessorPorIdCutover(InternalRequestContext context);

    Optional<ResumoResponse> buscarProfessorPorId(InternalRequestContext context, UUID professorId);

    boolean supportsListarAlocacoes(InternalRequestContext context, UUID professorId);

    List<AlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId);

    boolean supportsListarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);

    List<AlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId);
}

