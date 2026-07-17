package br.com.escola.catalog.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.escola.catalog.application.migration.MigracaoReport;
import br.com.escola.catalog.application.migration.MigracaoSnapshot;
import br.com.escola.catalog.application.port.out.MigracaoOrigemPort;
import br.com.escola.catalog.application.port.out.MigracaoDestinoPort;
import br.com.escola.catalog.application.port.out.LeituraCachePort;
import br.com.escola.catalog.domain.valueobject.EscolaId;

@Service
@ConditionalOnProperty(name = "catalog.migration.enabled", havingValue = "true")
public class MigracaoService {

    private final MigracaoOrigemPort sourcePort;
    private final MigracaoDestinoPort targetPort;
    private final LeituraCachePort cachePort;
    private final Clock clock;

    @Autowired
    public MigracaoService(
            MigracaoOrigemPort sourcePort,
            MigracaoDestinoPort targetPort,
            LeituraCachePort cachePort) {
        this(sourcePort, targetPort, cachePort, Clock.systemUTC());
    }

    MigracaoService(
            MigracaoOrigemPort sourcePort,
            MigracaoDestinoPort targetPort,
            LeituraCachePort cachePort,
            Clock clock) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.cachePort = cachePort;
        this.clock = clock;
    }

    public MigracaoReport executar(boolean apply) {
        Instant startedAt = clock.instant();
        MigracaoSnapshot source = sourcePort.carregarSnapshot();
        MigracaoSnapshot targetBefore = targetPort.carregarSnapshot();
        List<String> sourceIssues = validarOrigem(source);
        List<String> targetIssues = validarColisoesDestino(source, targetBefore);
        boolean applied = apply && sourceIssues.isEmpty() && targetIssues.isEmpty();
        if (applied) {
            targetPort.aplicar(source);
            source.periodos().stream().map(MigracaoSnapshot.PeriodoLetivoRow::escolaId)
                    .distinct().map(EscolaId::new).forEach(cachePort::invalidar);
        }
        MigracaoSnapshot target = applied ? targetPort.carregarSnapshot() : targetBefore;
        List<MigracaoReport.TableReport> tables = reconciliar(source, target);
        boolean reconciled = sourceIssues.isEmpty() && targetIssues.isEmpty()
                && tables.stream().allMatch(MigracaoReport.TableReport::reconciled);
        return new MigracaoReport(
                startedAt, clock.instant(), apply, applied, reconciled, sourceIssues, targetIssues, tables);
    }

    private List<String> validarColisoesDestino(
            MigracaoSnapshot source,
            MigracaoSnapshot target) {
        List<String> issues = new ArrayList<>();
        boolean allowGlobalSeedReplacement = targetHasNoSchoolData(target);
        if (!allowGlobalSeedReplacement) {
            findCollisions("nivel_ensino", source.niveisEnsino(), target.niveisEnsino(),
                    MigracaoSnapshot.NivelEnsinoRow::id,
                    MigracaoSnapshot.NivelEnsinoRow::codigo, issues);
            findCollisions("turno", source.turnos(), target.turnos(),
                    MigracaoSnapshot.TurnoRow::id,
                    MigracaoSnapshot.TurnoRow::codigo, issues);
        }
        findCollisions("periodo_letivo", source.periodos(), target.periodos(),
                MigracaoSnapshot.PeriodoLetivoRow::id,
                row -> row.escolaId() + "|" + row.nome(), issues);
        findCollisions("serie", source.series(), target.series(),
                MigracaoSnapshot.SerieRow::id,
                row -> row.escolaId() + "|" + row.nome(), issues);
        findCollisions("disciplina", source.disciplinas(), target.disciplinas(),
                MigracaoSnapshot.DisciplinaRow::id,
                row -> row.escolaId() + "|" + row.nome(), issues);
        findCollisions("turma", source.turmas(), target.turmas(),
                MigracaoSnapshot.TurmaRow::id,
                row -> row.escolaId() + "|" + row.periodoLetivoId() + "|" + row.codigo(), issues);
        findCollisions("turma_disciplina", source.turmaDisciplinas(), target.turmaDisciplinas(),
                MigracaoSnapshot.TurmaDisciplinaRow::id,
                row -> row.turmaId() + "|" + row.disciplinaId(), issues);
        return issues.stream().sorted().toList();
    }

    private <T> void findCollisions(
            String table,
            List<T> source,
            List<T> target,
            Function<T, UUID> id,
            Function<T, ?> naturalKey,
            List<String> issues) {
        Map<Object, UUID> targetByNaturalKey = new LinkedHashMap<>();
        target.forEach(row -> targetByNaturalKey.put(naturalKey.apply(row), id.apply(row)));
        source.forEach(row -> {
            UUID targetId = targetByNaturalKey.get(naturalKey.apply(row));
            if (targetId != null && !targetId.equals(id.apply(row))) {
                issues.add(table + ":chave_natural_com_outro_id:origem=" + id.apply(row)
                        + ":destino=" + targetId);
            }
        });
    }

    private List<String> validarOrigem(MigracaoSnapshot source) {
        List<String> issues = new ArrayList<>();
        var niveis = source.niveisEnsino().stream().map(MigracaoSnapshot.NivelEnsinoRow::id)
                .collect(java.util.stream.Collectors.toSet());
        var periodos = source.periodos().stream().collect(java.util.stream.Collectors.toMap(
                MigracaoSnapshot.PeriodoLetivoRow::id,
                MigracaoSnapshot.PeriodoLetivoRow::escolaId));
        var series = source.series().stream().collect(java.util.stream.Collectors.toMap(
                MigracaoSnapshot.SerieRow::id, MigracaoSnapshot.SerieRow::escolaId));
        var turnos = source.turnos().stream().map(MigracaoSnapshot.TurnoRow::id)
                .collect(java.util.stream.Collectors.toSet());
        var disciplinas = source.disciplinas().stream().collect(java.util.stream.Collectors.toMap(
                MigracaoSnapshot.DisciplinaRow::id,
                MigracaoSnapshot.DisciplinaRow::escolaId));
        var turmas = source.turmas().stream().collect(java.util.stream.Collectors.toMap(
                MigracaoSnapshot.TurmaRow::id, MigracaoSnapshot.TurmaRow::escolaId));

        source.series().forEach(row -> {
            if (row.nivelEnsinoId() == null || !niveis.contains(row.nivelEnsinoId())) {
                issues.add("serie:" + row.id() + ":nivel_ensino_invalido");
            }
        });
        source.turmas().forEach(row -> {
            if (row.turnoId() == null || !turnos.contains(row.turnoId())) {
                issues.add("turma:" + row.id() + ":turno_invalido");
            }
            if (!java.util.Objects.equals(periodos.get(row.periodoLetivoId()), row.escolaId())) {
                issues.add("turma:" + row.id() + ":periodo_de_outra_escola");
            }
            if (!java.util.Objects.equals(series.get(row.serieId()), row.escolaId())) {
                issues.add("turma:" + row.id() + ":serie_de_outra_escola");
            }
        });
        source.turmaDisciplinas().forEach(row -> {
            if (!java.util.Objects.equals(turmas.get(row.turmaId()), row.escolaId())) {
                issues.add("turma_disciplina:" + row.id() + ":turma_de_outra_escola");
            }
            if (!java.util.Objects.equals(disciplinas.get(row.disciplinaId()), row.escolaId())) {
                issues.add("turma_disciplina:" + row.id() + ":disciplina_de_outra_escola");
            }
        });
        return issues.stream().sorted().toList();
    }

    private boolean targetHasNoSchoolData(MigracaoSnapshot target) {
        return target.periodos().isEmpty()
                && target.series().isEmpty()
                && target.disciplinas().isEmpty()
                && target.turmas().isEmpty()
                && target.turmaDisciplinas().isEmpty();
    }

    private List<MigracaoReport.TableReport> reconciliar(
            MigracaoSnapshot source,
            MigracaoSnapshot target) {
        List<MigracaoReport.TableReport> reports = new ArrayList<>();
        reports.add(compareGlobal("nivel_ensino", source.niveisEnsino(), target.niveisEnsino(),
                MigracaoSnapshot.NivelEnsinoRow::id));
        reports.add(compareGlobal("turno", source.turnos(), target.turnos(),
                MigracaoSnapshot.TurnoRow::id));
        reports.addAll(compareBySchool("periodo_letivo", source.periodos(), target.periodos(),
                MigracaoSnapshot.PeriodoLetivoRow::id,
                MigracaoSnapshot.PeriodoLetivoRow::escolaId));
        reports.addAll(compareBySchool("serie", source.series(), target.series(),
                MigracaoSnapshot.SerieRow::id, MigracaoSnapshot.SerieRow::escolaId));
        reports.addAll(compareBySchool("disciplina", source.disciplinas(), target.disciplinas(),
                MigracaoSnapshot.DisciplinaRow::id,
                MigracaoSnapshot.DisciplinaRow::escolaId));
        reports.addAll(compareBySchool("turma", source.turmas(), target.turmas(),
                MigracaoSnapshot.TurmaRow::id, MigracaoSnapshot.TurmaRow::escolaId));
        reports.addAll(compareBySchool("turma_disciplina", source.turmaDisciplinas(), target.turmaDisciplinas(),
                MigracaoSnapshot.TurmaDisciplinaRow::id,
                MigracaoSnapshot.TurmaDisciplinaRow::escolaId));
        return reports;
    }

    private <T> MigracaoReport.TableReport compareGlobal(
            String table, List<T> source, List<T> target, Function<T, UUID> id) {
        return compare(table, null, source, target, id);
    }

    private <T> List<MigracaoReport.TableReport> compareBySchool(
            String table,
            List<T> source,
            List<T> target,
            Function<T, UUID> id,
            Function<T, UUID> school) {
        Map<UUID, List<T>> sourceBySchool = groupBySchool(source, school);
        Map<UUID, List<T>> targetBySchool = groupBySchool(target, school);
        return java.util.stream.Stream.concat(sourceBySchool.keySet().stream(), targetBySchool.keySet().stream())
                .distinct().sorted().map(escolaId -> compare(
                        table, escolaId,
                        sourceBySchool.getOrDefault(escolaId, List.of()),
                        targetBySchool.getOrDefault(escolaId, List.of()), id))
                .toList();
    }

    private <T> Map<UUID, List<T>> groupBySchool(List<T> rows, Function<T, UUID> school) {
        Map<UUID, List<T>> grouped = new LinkedHashMap<>();
        rows.forEach(row -> grouped.computeIfAbsent(school.apply(row), ignored -> new ArrayList<>()).add(row));
        return grouped;
    }

    private <T> MigracaoReport.TableReport compare(
            String table,
            UUID escolaId,
            List<T> source,
            List<T> target,
            Function<T, UUID> id) {
        Map<UUID, T> sourceById = index(source, id);
        Map<UUID, T> targetById = index(target, id);
        List<UUID> missing = sourceById.keySet().stream().filter(key -> !targetById.containsKey(key)).sorted().toList();
        List<UUID> unexpected = targetById.keySet().stream().filter(key -> !sourceById.containsKey(key)).sorted().toList();
        List<UUID> divergent = sourceById.keySet().stream()
                .filter(targetById::containsKey)
                .filter(key -> !sourceById.get(key).equals(targetById.get(key)))
                .sorted().toList();
        return new MigracaoReport.TableReport(
                table, escolaId, source.size(), target.size(), missing, unexpected, divergent);
    }

    private <T> Map<UUID, T> index(List<T> rows, Function<T, UUID> id) {
        Map<UUID, T> indexed = new LinkedHashMap<>();
        rows.stream().sorted(Comparator.comparing(id)).forEach(row -> {
            UUID key = id.apply(row);
            if (indexed.put(key, row) != null) {
                throw new IllegalStateException("ID duplicado no snapshot de migracao: " + key);
            }
        });
        return indexed;
    }
}

