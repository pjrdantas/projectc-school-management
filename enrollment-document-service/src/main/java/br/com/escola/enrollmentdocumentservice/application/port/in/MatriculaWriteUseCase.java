package br.com.escola.enrollmentdocumentservice.application.port.in;

import java.util.UUID;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarStatusMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CancelarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;

public interface MatriculaWriteUseCase {

    MatriculaResponse criar(CriarMatriculaCommand command, InternalRequestContext context);

    MatriculaResponse atualizar(UUID matriculaId, AtualizarMatriculaCommand command, InternalRequestContext context);

    MatriculaResponse atualizarStatus(
            UUID matriculaId,
            AtualizarStatusMatriculaCommand command,
            InternalRequestContext context);

    MatriculaResponse cancelar(UUID matriculaId, CancelarMatriculaCommand command, InternalRequestContext context);
}
