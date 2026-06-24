package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorCreateRequest;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorWritePort {

    ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request);
}
