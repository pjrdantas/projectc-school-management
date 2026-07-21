package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.util.UUID;
import java.util.Optional;

import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarStatusMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CancelarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;

public interface MatriculaWritePort {

    Optional<MatriculaResponse> buscar(UUID matriculaId, UUID escolaId);

    MatriculaResponse criar(CriarMatriculaCommand command, UUID escolaId);

    MatriculaResponse atualizar(UUID matriculaId, AtualizarMatriculaCommand command, UUID escolaId);

    MatriculaResponse atualizarStatus(UUID matriculaId, AtualizarStatusMatriculaCommand command, UUID escolaId);

    MatriculaResponse cancelar(UUID matriculaId, CancelarMatriculaCommand command, UUID escolaId);
}
