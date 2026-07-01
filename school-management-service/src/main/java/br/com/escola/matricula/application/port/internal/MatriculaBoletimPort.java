package br.com.escola.matricula.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.matricula.application.dto.internal.MatriculaBoletimResumo;

public interface MatriculaBoletimPort {

    Optional<MatriculaBoletimResumo> buscarResumoPorIdEEscola(UUID matriculaId, UUID escolaId);
}
