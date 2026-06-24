package br.com.escola.professorservice.application.port.in;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorCreateRequest;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorCommandUseCase {

    ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request);
}
