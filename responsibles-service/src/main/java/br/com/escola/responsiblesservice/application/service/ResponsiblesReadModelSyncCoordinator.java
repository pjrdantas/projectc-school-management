package br.com.escola.responsiblesservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.responsiblesservice.application.port.out.ResponsiblesReadModelSyncPort;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Service
public class ResponsiblesReadModelSyncCoordinator {

    private final ResponsiblesReadModelProperties properties;
    private final ResponsiblesReadModelSyncPort syncPort;

    public ResponsiblesReadModelSyncCoordinator(
            ResponsiblesReadModelProperties properties,
            ResponsiblesReadModelSyncPort syncPort) {
        this.properties = properties;
        this.syncPort = syncPort;
    }

    public ResponsiblesReadModelSyncSummary executarCicloControlado() {
        if (!properties.backfillEnabled() && !properties.reconciliationEnabled()) {
            return report(false, false, "disabled", "backfill-and-reconciliation-disabled", List.of());
        }

        List<TableOperationReport> tableReports;
        try {
            tableReports = syncPort.synchronize(
                    properties.backfillEnabled(),
                    properties.reconciliationEnabled(),
                    properties.backfillBatchSize());
        } catch (RuntimeException exception) {
            if (properties.failOnError()) {
                throw exception;
            }
            return report(
                    properties.backfillEnabled(),
                    properties.reconciliationEnabled(),
                    "failed",
                    "responsibles-read-model-sync-failed",
                    List.of());
        }

        String status = tableReports.stream().anyMatch(report -> !"success".equals(report.status())) ? "blocked" : "completed";
        String reason = "completed".equals(status)
                ? (properties.reconciliationEnabled()
                        ? "responsibles-read-model-backfill-and-reconciliation-completed"
                        : "responsibles-read-model-backfill-completed")
                : tableReports.stream()
                        .filter(report -> !"success".equals(report.status()))
                        .map(TableOperationReport::reason)
                        .findFirst()
                        .orElse(status);

        if ("blocked".equals(status) && properties.failOnError()) {
            throw new IllegalStateException(reason);
        }
        return report(properties.backfillEnabled(), properties.reconciliationEnabled(), status, reason, tableReports);
    }

    private ResponsiblesReadModelSyncSummary report(
            boolean backfillEnabled,
            boolean reconciliationEnabled,
            String status,
            String reason,
            List<TableOperationReport> tableReports) {
        return new ResponsiblesReadModelSyncSummary(
                backfillEnabled,
                reconciliationEnabled,
                status,
                reason,
                properties.backfillBatchSize(),
                tableReports.size(),
                (int) tableReports.stream().filter(report -> "success".equals(report.status())).count(),
                tableReports.stream().mapToInt(TableOperationReport::backfilledRecords).sum(),
                tableReports.stream().mapToInt(TableOperationReport::divergentRecords).sum(),
                tableReports.stream().mapToInt(TableOperationReport::sourceRows).sum(),
                tableReports.stream().mapToInt(TableOperationReport::targetRows).sum(),
                tableReports);
    }
}
