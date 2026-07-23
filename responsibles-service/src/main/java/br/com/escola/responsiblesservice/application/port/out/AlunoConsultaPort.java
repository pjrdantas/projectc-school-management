package br.com.escola.responsiblesservice.application.port.out;

import java.util.UUID;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;

public interface AlunoConsultaPort {

    boolean existeAtivoNaEscola(UUID alunoId, InternalRequestContext context);
}
