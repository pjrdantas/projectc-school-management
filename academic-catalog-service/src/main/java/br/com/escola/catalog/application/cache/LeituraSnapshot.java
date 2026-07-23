package br.com.escola.catalog.application.cache;

import java.util.List;

import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.NivelEnsinoResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;

public record LeituraSnapshot(
        List<NivelEnsinoResponse> niveisEnsino,
        List<TurnoResponse> turnos,
        List<PeriodoLetivoResponse> periodos,
        List<SerieResponse> series,
        List<TurmaResponse> turmas,
        List<DisciplinaResponse> disciplinas,
        List<TurmaDisciplinaResponse> turmaDisciplinas) {

    public LeituraSnapshot {
        niveisEnsino = List.copyOf(niveisEnsino);
        turnos = List.copyOf(turnos);
        periodos = List.copyOf(periodos);
        series = List.copyOf(series);
        turmas = List.copyOf(turmas);
        disciplinas = List.copyOf(disciplinas);
        turmaDisciplinas = List.copyOf(turmaDisciplinas);
    }
}

