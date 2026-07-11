package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary.TableOperationReport;
import br.com.escola.peopleservice.application.port.out.PeopleCatalogReadModelSyncPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleReadModelSyncCoordinator {

    private static final List<TableDescriptor> TABLES = List.of(
            new TableDescriptor("tipo_pessoa", "id_tipo_pessoa"),
            new TableDescriptor("tipo_endereco", "id_tipo_endereco"),
            new TableDescriptor("pessoa", "id_pessoa"),
            new TableDescriptor("pessoa_tipo_pessoa", "id_pessoa_tipo_pessoa"),
            new TableDescriptor("aluno", "id_aluno"),
            new TableDescriptor("responsavel", "id_responsavel"),
            new TableDescriptor("aluno_responsavel", "id_aluno_responsavel"),
            new TableDescriptor("endereco", "id_endereco"),
            new TableDescriptor("pessoa_endereco", "id_pessoa_endereco"),
            new TableDescriptor("people_documento_read_model", "id_pessoa_documento"),
            new TableDescriptor("people_funcionario_read_model", "id_funcionario"));

    private final PeopleReadModelProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleCatalogReadModelSyncPort catalogSyncPort;
    private final PeopleReadModelSyncState operationState;

    public PeopleReadModelSyncCoordinator(
            PeopleReadModelProperties properties,
            MeterRegistry meterRegistry,
            PeopleCatalogReadModelSyncPort catalogSyncPort,
            PeopleReadModelSyncState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.catalogSyncPort = catalogSyncPort;
        this.operationState = operationState;
    }

    public PeopleReadModelSyncSummary executarCicloControlado() {
        boolean backfillEnabled = properties.backfillEnabled();
        boolean reconciliationEnabled = properties.reconciliationEnabled();
        boolean operationEnabled = backfillEnabled || reconciliationEnabled;

        if (!operationEnabled) {
            PeopleReadModelSyncSummary report = report(
                    backfillEnabled,
                    reconciliationEnabled,
                    "disabled",
                    "operation-flags-disabled",
                    List.of());
            operationState.update(report);
            return report;
        }

        List<TableOperationReport> tableReports;
        try {
            tableReports = catalogSyncPort.synchronize(
                    backfillEnabled,
                    reconciliationEnabled,
                    properties.backfillBatchSize());
        } catch (RuntimeException ex) {
            incrementar("people.readmodel.sync.cycles", "status", "failed");
            incrementar("people.readmodel.sync.failures", "reason", "local-read-model-sync-failed");
            PeopleReadModelSyncSummary report = report(
                    backfillEnabled,
                    reconciliationEnabled,
                    "failed",
                    "local-read-model-sync-failed",
                    List.of());
            operationState.update(report);
            if (properties.failOnError()) {
                throw ex;
            }
            return report;
        }

        String status = aggregateStatus(tableReports);
        String reason = aggregateReason(status, tableReports);
        incrementar("people.readmodel.sync.cycles", "status", status);
        if (backfillEnabled) {
            incrementar("people.readmodel.sync.tables.planned", TABLES.size());
            incrementar("people.readmodel.sync.records",
                    tableReports.stream().mapToInt(TableOperationReport::backfilledRecords).sum());
        }
        if (reconciliationEnabled) {
            incrementar("people.readmodel.sync.reconciliation.tables", TABLES.size());
            tableReports.stream()
                    .filter(table -> table.divergences() > 0)
                    .forEach(table -> incrementar(
                            "people.readmodel.sync.divergences",
                            "tabela",
                            table.table(),
                            table.divergences()));
        }
        if ("blocked".equals(status) || "failed".equals(status)) {
            incrementar("people.readmodel.sync.failures", "reason", reason);
        }

        PeopleReadModelSyncSummary report = report(
                backfillEnabled,
                reconciliationEnabled,
                status,
                reason,
                tableReports);
        operationState.update(report);
        if (("blocked".equals(status) || "failed".equals(status)) && properties.failOnError()) {
            throw new IllegalStateException(reason);
        }
        return report;
    }

    private PeopleReadModelSyncSummary report(
            boolean backfillEnabled,
            boolean reconciliationEnabled,
            String status,
            String reason,
            List<TableOperationReport> tableReports) {
        return new PeopleReadModelSyncSummary(
                backfillEnabled,
                reconciliationEnabled,
                status,
                reason,
                properties.backfillBatchSize(),
                TABLES.size(),
                (int) tableReports.stream().filter(table -> "success".equals(table.status())).count(),
                tableReports.stream().mapToInt(TableOperationReport::backfilledRecords).sum(),
                tableReports.stream().mapToInt(TableOperationReport::sourceRows).sum(),
                tableReports.stream().mapToInt(TableOperationReport::targetRows).sum(),
                tableReports.stream().mapToInt(TableOperationReport::divergences).sum(),
                false,
                false,
                tableReports);
    }

    private String aggregateStatus(List<TableOperationReport> tableReports) {
        if (tableReports.stream().anyMatch(table -> "blocked".equals(table.status()))) {
            return "blocked";
        }
        if (tableReports.stream().anyMatch(table -> "failed".equals(table.status()))) {
            return "failed";
        }
        if (tableReports.stream().anyMatch(table -> table.divergences() > 0 || "diverged".equals(table.status()))) {
            return "diverged";
        }
        return "completed";
    }

    private String aggregateReason(String status, List<TableOperationReport> tableReports) {
        if ("completed".equals(status)) {
            return "local-read-model-backfill-and-reconciliation-completed";
        }
        return tableReports.stream()
                .filter(table -> !"success".equals(table.status()))
                .map(TableOperationReport::reason)
                .findFirst()
                .orElse(status);
    }

    private void incrementar(String metricName, String tagKey, String tagValue) {
        Counter.builder(metricName)
                .tag(tagKey, tagValue)
                .register(meterRegistry)
                .increment();
    }

    private void incrementar(String metricName, int quantidade) {
        Counter.builder(metricName)
                .register(meterRegistry)
                .increment(quantidade);
    }

    private void incrementar(String metricName, String tagKey, String tagValue, int quantidade) {
        Counter.builder(metricName)
                .tag(tagKey, tagValue)
                .register(meterRegistry)
                .increment(quantidade);
    }

    private record TableDescriptor(String name, String keyColumn) {
    }
}

