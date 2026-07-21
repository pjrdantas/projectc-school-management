package br.com.escola.peopleservice.application.port.in;

import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;

public interface ExcluirAlunoUseCase {

    void excluir(UUID alunoId, InternalRequestContext context);
}
