package br.com.escola.catalogo.application.port.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalogo.application.dto.internal.DisciplinaResumo;
import br.com.escola.catalogo.application.dto.internal.PeriodoLetivoResumo;
import br.com.escola.catalogo.application.dto.internal.SerieResumo;
import br.com.escola.catalogo.application.dto.internal.TurmaResumo;
import br.com.escola.catalogo.application.dto.internal.TurnoResumo;

public interface CatalogoAcademicoPort {

    List<PeriodoLetivoResumo> listarPeriodosLetivos(UUID escolaId);

    Optional<PeriodoLetivoResumo> buscarPeriodoLetivo(UUID escolaId, UUID periodoLetivoId);

    boolean existePeriodoLetivo(UUID escolaId, UUID periodoLetivoId);

    List<SerieResumo> listarSeries(UUID escolaId);

    Optional<SerieResumo> buscarSerie(UUID escolaId, UUID serieId);

    boolean existeSerie(UUID escolaId, UUID serieId);

    List<TurnoResumo> listarTurnos();

    List<DisciplinaResumo> listarDisciplinas(UUID escolaId);

    Optional<DisciplinaResumo> buscarDisciplina(UUID escolaId, UUID disciplinaId);

    boolean existeDisciplina(UUID escolaId, UUID disciplinaId);

    List<TurmaResumo> listarTurmas(UUID escolaId);

    Optional<TurmaResumo> buscarTurma(UUID escolaId, UUID turmaId);

    boolean existeTurma(UUID escolaId, UUID turmaId);
}
