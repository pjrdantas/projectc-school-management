package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.util.UUID;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;

public interface AlunoMatriculaPort {

    boolean existeAtivoNaEscola(UUID alunoId, InternalRequestContext context);
}
