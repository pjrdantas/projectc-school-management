package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAllocateRequest;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;

public interface ProfessorShadowPersistencePort {

    void registrarCriacaoShadow(InternalRequestContext context, ProfessorResumoResponse response);

    void registrarAlocacaoShadow(
            InternalRequestContext context,
            ProfessorAllocateRequest request,
            ProfessorAlocacaoResponse response);
}
