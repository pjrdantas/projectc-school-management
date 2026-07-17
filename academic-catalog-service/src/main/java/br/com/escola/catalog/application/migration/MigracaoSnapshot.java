package br.com.escola.catalog.application.migration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MigracaoSnapshot(
        List<NivelEnsinoRow> niveisEnsino,
        List<TurnoRow> turnos,
        List<PeriodoLetivoRow> periodos,
        List<SerieRow> series,
        List<DisciplinaRow> disciplinas,
        List<TurmaRow> turmas,
        List<TurmaDisciplinaRow> turmaDisciplinas) {

    public MigracaoSnapshot {
        niveisEnsino = List.copyOf(niveisEnsino);
        turnos = List.copyOf(turnos);
        periodos = List.copyOf(periodos);
        series = List.copyOf(series);
        disciplinas = List.copyOf(disciplinas);
        turmas = List.copyOf(turmas);
        turmaDisciplinas = List.copyOf(turmaDisciplinas);
    }

    public record NivelEnsinoRow(UUID id, String codigo, String descricao) {}

    public record TurnoRow(UUID id, String codigo, String descricao) {}

    public record PeriodoLetivoRow(
            UUID id, UUID escolaId, String nome, Integer ano, LocalDate dataInicio,
            LocalDate dataFim, boolean ativo, LocalDateTime createdAt) {}

    public record SerieRow(
            UUID id, UUID escolaId, String nome, Integer ordem, UUID nivelEnsinoId,
            LocalDateTime createdAt) {}

    public record DisciplinaRow(
            UUID id, UUID escolaId, String nome, Integer cargaHoraria, boolean ativo,
            LocalDateTime createdAt) {}

    public record TurmaRow(
            UUID id, UUID escolaId, String codigo, String nome, Integer capacidade,
            UUID periodoLetivoId, UUID serieId, UUID turnoId, boolean ativo,
            LocalDateTime createdAt) {}

    public record TurmaDisciplinaRow(
            UUID id, UUID escolaId, UUID turmaId, UUID disciplinaId,
            Integer cargaHoraria, LocalDateTime createdAt) {}
}

