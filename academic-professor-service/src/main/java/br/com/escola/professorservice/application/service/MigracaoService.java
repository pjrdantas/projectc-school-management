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

import br.com.escola.professorservice.application.migration.MigracaoReport;
import br.com.escola.professorservice.application.migration.MigracaoSnapshot;
import br.com.escola.professorservice.application.port.out.MigracaoOrigemPort;
import br.com.escola.professorservice.application.port.out.MigracaoDestinoPort;

@Service
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class MigracaoService {

    private final MigracaoOrigemPort sourcePort;
    private final MigracaoDestinoPort targetPort;
    private final Clock clock;

    @Autowired
    public MigracaoService(
            MigracaoOrigemPort sourcePort,
            MigracaoDestinoPort targetPort) {
        this(sourcePort, targetPort, Clock.systemUTC());
    }

    MigracaoService(
            MigracaoOrigemPort sourcePort,
            MigracaoDestinoPort targetPort,
            Clock clock) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
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
        }
        MigracaoSnapshot target = applied ? targetPort.carregarSnapshot() : targetBefore;
        List<MigracaoReport.TableReport> tables = reconciliar(source, target);
        boolean reconciled = sourceIssues.isEmpty() && targetIssues.isEmpty()
                && tables.stream().allMatch(MigracaoReport.TableReport::reconciled);
        return new MigracaoReport(
                startedAt, clock.instant(), apply, applied, reconciled, sourceIssues, targetIssues, tables);
    }

    private List<String> validarOrigem(MigracaoSnapshot source) {
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
                MigracaoSnapshot.CadastroRow::id,
                issues);
        validarDuplicidades(
                "professor_por_pessoa_escola",
                source.professores(),
                row -> row.pessoaId() + "|" + row.escolaId(),
                issues);
        return issues.stream().sorted().toList();
    }

    private List<String> validarColisoesDestino(
            MigracaoSnapshot source,
            MigracaoSnapshot target) {
        List<String> issues = new ArrayList<>();
        Map<UUID, MigracaoSnapshot.CadastroRow> sourceById = indexar(
                source.professores(), MigracaoSnapshot.CadastroRow::id);
        for (MigracaoSnapshot.CadastroRow targetRow : target.professores()) {
            MigracaoSnapshot.CadastroRow sourceRow = sourceById.get(targetRow.id());
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
            List<MigracaoSnapshot.CadastroRow> rows,
            Function<MigracaoSnapshot.CadastroRow, Object> classifier,
            List<String> issues) {
        Map<Object, List<MigracaoSnapshot.CadastroRow>> grouped = new LinkedHashMap<>();
        for (MigracaoSnapshot.CadastroRow row : rows) {
            grouped.computeIfAbsent(classifier.apply(row), ignored -> new ArrayList<>()).add(row);
        }
        grouped.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> issuePrefix + ":" + entry.getKey())
                .sorted()
                .forEach(issues::add);
    }

    private List<MigracaoReport.TableReport> reconciliar(
            MigracaoSnapshot source,
            MigracaoSnapshot target) {
        Map<UUID, List<MigracaoSnapshot.CadastroRow>> sourceByEscola = agruparPorEscola(source.professores());
        Map<UUID, List<MigracaoSnapshot.CadastroRow>> targetByEscola = agruparPorEscola(target.professores());
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

    private MigracaoReport.TableReport reconciliarTabela(
            String table,
            UUID escolaId,
            List<MigracaoSnapshot.CadastroRow> sourceRows,
            List<MigracaoSnapshot.CadastroRow> targetRows) {
        Map<UUID, MigracaoSnapshot.CadastroRow> sourceById = indexar(
                sourceRows, MigracaoSnapshot.CadastroRow::id);
        Map<UUID, MigracaoSnapshot.CadastroRow> targetById = indexar(
                targetRows, MigracaoSnapshot.CadastroRow::id);

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

        return new MigracaoReport.TableReport(
                table,
                escolaId,
                sourceRows.size(),
                targetRows.size(),
                missingIds,
                unexpectedIds,
                divergentIds);
    }

    private boolean equivalentes(
            MigracaoSnapshot.CadastroRow source,
            MigracaoSnapshot.CadastroRow target) {
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

    private Map<UUID, MigracaoSnapshot.CadastroRow> indexar(
            List<MigracaoSnapshot.CadastroRow> rows,
            Function<MigracaoSnapshot.CadastroRow, UUID> classifier) {
        Map<UUID, MigracaoSnapshot.CadastroRow> indexed = new LinkedHashMap<>();
        for (MigracaoSnapshot.CadastroRow row : rows) {
            indexed.put(classifier.apply(row), row);
        }
        return indexed;
    }

    private Map<UUID, List<MigracaoSnapshot.CadastroRow>> agruparPorEscola(
            List<MigracaoSnapshot.CadastroRow> rows) {
        Map<UUID, List<MigracaoSnapshot.CadastroRow>> grouped = new LinkedHashMap<>();
        for (MigracaoSnapshot.CadastroRow row : rows) {
            grouped.computeIfAbsent(row.escolaId(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }
}


