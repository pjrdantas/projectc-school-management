package br.com.escola.peopleservice.application.port.in;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoCriacaoRequest;
import br.com.escola.peopleservice.application.model.AlunoCriado;

public interface CriarAlunoUseCase {

    AlunoCriado criar(
            AlunoCriacaoRequest request,
            String authorization,
            InternalRequestContext context);
}
