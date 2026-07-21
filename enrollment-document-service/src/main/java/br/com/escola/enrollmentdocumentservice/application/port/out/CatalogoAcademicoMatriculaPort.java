package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.util.UUID;
import java.util.Optional;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.TurmaMatriculaResumo;

public interface CatalogoAcademicoMatriculaPort {

    Optional<TurmaMatriculaResumo> buscarTurmaNaEscola(UUID turmaId, InternalRequestContext context);

    boolean existeSerieNaEscola(UUID serieId, InternalRequestContext context);

    boolean existePeriodoLetivoAtivoNaEscola(UUID periodoLetivoId, InternalRequestContext context);
}
