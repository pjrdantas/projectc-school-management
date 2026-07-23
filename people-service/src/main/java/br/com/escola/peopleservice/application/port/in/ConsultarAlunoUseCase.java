package br.com.escola.peopleservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoFichaResponse;
import br.com.escola.peopleservice.application.dto.AlunoResponse;

public interface ConsultarAlunoUseCase {

    List<AlunoResponse> listar(String nome, InternalRequestContext context);

    AlunoResponse buscar(UUID alunoId, InternalRequestContext context);

    AlunoFichaResponse buscarFicha(
            UUID alunoId,
            String authorization,
            InternalRequestContext context);
}
