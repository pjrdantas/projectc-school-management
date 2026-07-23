package br.com.escola.peopleservice.application.port.in;

import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoAtualizacaoRequest;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;

public interface AtualizarAlunoUseCase {

    AlunoAtualizado atualizar(
            UUID alunoId,
            AlunoAtualizacaoRequest request,
            InternalRequestContext context);
}
