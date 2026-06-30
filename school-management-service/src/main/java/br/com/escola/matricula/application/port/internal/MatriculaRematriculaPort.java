package br.com.escola.matricula.application.port.internal;

import java.util.UUID;

import br.com.escola.matricula.application.dto.internal.MatriculaRematriculaBaseResumo;
import br.com.escola.matricula.application.dto.internal.MatriculaRematriculaElegibilidadeResumo;

public interface MatriculaRematriculaPort {

    MatriculaRematriculaBaseResumo buscarBaseParaRematricula(UUID matriculaAnteriorId);

    MatriculaRematriculaElegibilidadeResumo consultarElegibilidade(
            UUID matriculaAnteriorId,
            UUID turmaDestinoId,
            UUID periodoLetivoDestinoId);
}
