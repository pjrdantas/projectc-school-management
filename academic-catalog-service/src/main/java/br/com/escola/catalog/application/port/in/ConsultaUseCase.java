package br.com.escola.catalog.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.NivelEnsinoResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;

public interface ConsultaUseCase {

    List<NivelEnsinoResponse> listarNiveisEnsino(InternalRequestContext context);

    List<TurnoResponse> listarTurnos(InternalRequestContext context);

    TurnoResponse buscarTurno(UUID id, InternalRequestContext context);

    List<PeriodoLetivoResponse> listarPeriodos(InternalRequestContext context);

    PeriodoLetivoResponse buscarPeriodo(UUID id, InternalRequestContext context);

    List<SerieResponse> listarSeries(InternalRequestContext context);

    SerieResponse buscarSerie(UUID id, InternalRequestContext context);

    List<TurmaResponse> listarTurmas(InternalRequestContext context);

    TurmaResponse buscarTurma(UUID id, InternalRequestContext context);

    List<DisciplinaResponse> listarDisciplinas(InternalRequestContext context);

    DisciplinaResponse buscarDisciplina(UUID id, InternalRequestContext context);

    TurmaDisciplinaResponse buscarTurmaDisciplina(UUID id, InternalRequestContext context);

    List<TurmaDisciplinaResponse> listarDisciplinasDaTurma(UUID turmaId, InternalRequestContext context);
}

