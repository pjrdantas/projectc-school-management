package br.com.escola.professorservice.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.migration.ProfessorShadowMigrationReport;
import br.com.escola.professorservice.application.migration.ProfessorShadowMigrationSnapshot;
import br.com.escola.professorservice.application.port.out.ProfessorShadowMigrationSourcePort;
import br.com.escola.professorservice.application.port.out.ProfessorShadowMigrationTargetPort;

@Service
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class ProfessorShadowMigrationService {

    private final ProfessorShadowMigrationSourcePort sourcePort;
    private final ProfessorShadowMigrationTargetPort targetPort;
    private final Clock clock;

    @Autowired
    public ProfessorShadowMigrationService(
            ProfessorShadowMigrationSourcePort sourcePort,
            ProfessorShadowMigrationTargetPort targetPort) {
        this(sourcePort, targetPort, Clock.systemUTC());
    }

    ProfessorShadowMigrationService(
            ProfessorShadowMigrationSourcePort sourcePort,
            ProfessorShadowMigrationTargetPort targetPort,
            Clock clock) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.clock = clock;
    }

    public ProfessorShadowMigrationReport executar(boolean apply) {
        Instant startedAt = clock.instant();
        ProfessorShadowMigrationSnapshot source = sourcePort.carregarSnapshot();
        ProfessorShadowMigrationSnapshot targetBefore = targetPort.carregarSnapshot();
        List<String> sourceIssues = validarOrigem(source);
        List<String> targetIssues = validarColisoesDestino(source, targetBefore);
        boolean applied = apply && sourceIssues.isEmpty() && targetIssues.isEmpty();
        if (applied) {
            targetPort.aplicar(source);
        }
        ProfessorShadowMigrationSnapshot target = applied ? targetPort.carregarSnapshot() : targetBefore;
        List<ProfessorShadowMigrationReport.TableReport> tables = reconciliar(source, target);
        boolean reconciled = sourceIssues.isEmpty() && targetIssues.isEmpty()
                && tables.stream().allMatch(ProfessorShadowMigrationReport.TableReport::reconciled);
        return new ProfessorShadowMigrationReport(
                startedAt, clock.instant(), apply, applied, reconciled, sourceIssues, targetIssues, tables);
    }

    private List<String> validarOrigem(ProfessorShadowMigrationSnapshot source) {
        List<String> issues = new ArrayList<>();
        source.professores().stream()
                .filter(row -> row.id() == null || row.pessoaId() == null || row.escolaId() == null
                        || row.nomeCompleto() == null || row.nomeCompleto().isBlank()
                        || row.createdAt() == null)
                .map(row -> "professor_invalido:" + row.id())
                .forEach(issues::add);
        validarDuplicidades(
                "professor_por_id",
                source.professores(),
                ProfessorShadowMigrationSnapshot.ProfessorRow::id,
                issues);
        validarDuplicidades(
                "professor_por_pessoa_escola",
                source.professores(),
                row -> row.pessoaId() + "|" + row.escolaId(),
                issues);
        return issues.stream().sorted().toList();
    }

    private List<String> validarColisoesDestino(
            ProfessorShadowMigrationSnapshot source,
            ProfessorShadowMigrationSnapshot target) {
        List<String> issues = new ArrayList<>();
        Map<UUID, ProfessorShadowMigrationSnapshot.ProfessorRow> sourceById = indexar(
                source.professores(), ProfessorShadowMigrationSnapshot.ProfessorRow::id);
        for (ProfessorShadowMigrationSnapshot.ProfessorRow targetRow : target.professores()) {
            ProfessorShadowMigrationSnapshot.ProfessorRow sourceRow = sourceById.get(targetRow.id());
            if (sourceRow == null) {
                continue;
            }
            if (!targetRow.pessoaId().equals(sourceRow.pessoaId())
                    || !targetRow.escolaId().equals(sourceRow.escolaId())) {
                issues.add("professor_destino_identidade_divergente:" + targetRow.id());
            }
        }
        return issues.stream().sorted().toList();
    }

    private void validarDuplicidades(
            String issuePrefix,
            List<ProfessorShadowMigrationSnapshot.ProfessorRow> rows,
            Function<ProfessorShadowMigrationSnapshot.ProfessorRow, Object> classifier,
            List<String> issues) {
        Map<Object, List<ProfessorShadowMigrationSnapshot.ProfessorRow>> grouped = new LinkedHashMap<>();
        for (ProfessorShadowMigrationSnapshot.ProfessorRow row : rows) {
            grouped.computeIfAbsent(classifier.apply(row), ignored -> new ArrayList<>()).add(row);
        }
        grouped.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> issuePrefix + ":" + entry.getKey())
                .sorted()
                .forEach(issues::add);
    }

    private List<ProfessorShadowMigrationReport.TableReport> reconciliar(
            ProfessorShadowMigrationSnapshot source,
            ProfessorShadowMigrationSnapshot target) {
        Map<UUID, List<ProfessorShadowMigrationSnapshot.ProfessorRow>> sourceByEscola = agruparPorEscola(source.professores());
        Map<UUID, List<ProfessorShadowMigrationSnapshot.ProfessorRow>> targetByEscola = agruparPorEscola(target.professores());
        List<UUID> escolas = new ArrayList<>();
        escolas.addAll(sourceByEscola.keySet());
        targetByEscola.keySet().stream()
                .filter(escolaId -> !sourceByEscola.containsKey(escolaId))
                .forEach(escolas::add);
        return escolas.stream()
                .sorted(Comparator.comparing(UUID::toString))
                .map(escolaId -> reconciliarTabela(
                        "professor",
                        escolaId,
                        sourceByEscola.getOrDefault(escolaId, List.of()),
                        targetByEscola.getOrDefault(escolaId, List.of())))
                .toList();
    }

    private ProfessorShadowMigrationReport.TableReport reconciliarTabela(
            String table,
            UUID escolaId,
            List<ProfessorShadowMigrationSnapshot.ProfessorRow> sourceRows,
            List<ProfessorShadowMigrationSnapshot.ProfessorRow> targetRows) {
        Map<UUID, ProfessorShadowMigrationSnapshot.ProfessorRow> sourceById = indexar(
                sourceRows, ProfessorShadowMigrationSnapshot.ProfessorRow::id);
        Map<UUID, ProfessorShadowMigrationSnapshot.ProfessorRow> targetById = indexar(
                targetRows, ProfessorShadowMigrationSnapshot.ProfessorRow::id);

        List<UUID> missingIds = sourceById.keySet().stream()
                .filter(id -> !targetById.containsKey(id))
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        List<UUID> unexpectedIds = targetById.keySet().stream()
                .filter(id -> !sourceById.containsKey(id))
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        List<UUID> divergentIds = sourceById.keySet().stream()
                .filter(targetById::containsKey)
                .filter(id -> !equivalentes(sourceById.get(id), targetById.get(id)))
                .sorted(Comparator.comparing(UUID::toString))
                .toList();

        return new ProfessorShadowMigrationReport.TableReport(
                table,
                escolaId,
                sourceRows.size(),
                targetRows.size(),
                missingIds,
                unexpectedIds,
                divergentIds);
    }

    private boolean equivalentes(
            ProfessorShadowMigrationSnapshot.ProfessorRow source,
            ProfessorShadowMigrationSnapshot.ProfessorRow target) {
        return source.id().equals(target.id())
                && source.pessoaId().equals(target.pessoaId())
                && source.escolaId().equals(target.escolaId())
                && nullSafeEquals(source.escolaNome(), target.escolaNome())
                && nullSafeEquals(source.nomeCompleto(), target.nomeCompleto())
                && nullSafeEquals(source.registroProfissional(), target.registroProfissional())
                && nullSafeEquals(source.formacao(), target.formacao())
                && source.ativo() == target.ativo()
                && nullSafeEquals(source.createdAt(), target.createdAt())
                && nullSafeEquals(source.updatedAt(), target.updatedAt())
                && nullSafeEquals(source.usuarioId(), target.usuarioId());
    }

    private boolean nullSafeEquals(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    private Map<UUID, ProfessorShadowMigrationSnapshot.ProfessorRow> indexar(
            List<ProfessorShadowMigrationSnapshot.ProfessorRow> rows,
            Function<ProfessorShadowMigrationSnapshot.ProfessorRow, UUID> classifier) {
        Map<UUID, ProfessorShadowMigrationSnapshot.ProfessorRow> indexed = new LinkedHashMap<>();
        for (ProfessorShadowMigrationSnapshot.ProfessorRow row : rows) {
            indexed.put(classifier.apply(row), row);
        }
        return indexed;
    }

    private Map<UUID, List<ProfessorShadowMigrationSnapshot.ProfessorRow>> agruparPorEscola(
            List<ProfessorShadowMigrationSnapshot.ProfessorRow> rows) {
        Map<UUID, List<ProfessorShadowMigrationSnapshot.ProfessorRow>> grouped = new LinkedHashMap<>();
        for (ProfessorShadowMigrationSnapshot.ProfessorRow row : rows) {
            grouped.computeIfAbsent(row.escolaId(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }
}
