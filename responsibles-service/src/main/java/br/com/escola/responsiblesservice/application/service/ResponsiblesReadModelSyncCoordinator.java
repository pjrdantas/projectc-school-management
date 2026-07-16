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
        if (!properties.backfillEnabled()) {
            return report(false, "disabled", "backfill-disabled", List.of());
        }

        List<TableOperationReport> tableReports;
        try {
            tableReports = syncPort.synchronize(true, properties.backfillBatchSize());
        } catch (RuntimeException exception) {
            if (properties.failOnError()) {
                throw exception;
            }
            return report(true, "failed", "responsibles-read-model-backfill-failed", List.of());
        }

        String status = tableReports.stream().anyMatch(report -> "blocked".equals(report.status())) ? "blocked" : "completed";
        String reason = "completed".equals(status)
                ? "responsibles-read-model-backfill-completed"
                : tableReports.stream()
                        .filter(report -> !"success".equals(report.status()))
                        .map(TableOperationReport::reason)
                        .findFirst()
                        .orElse(status);

        if ("blocked".equals(status) && properties.failOnError()) {
            throw new IllegalStateException(reason);
        }
        return report(true, status, reason, tableReports);
    }

    private ResponsiblesReadModelSyncSummary report(
            boolean backfillEnabled,
            String status,
            String reason,
            List<TableOperationReport> tableReports) {
        return new ResponsiblesReadModelSyncSummary(
                backfillEnabled,
                status,
                reason,
                properties.backfillBatchSize(),
                1,
                (int) tableReports.stream().filter(report -> "success".equals(report.status())).count(),
                tableReports.stream().mapToInt(TableOperationReport::backfilledRecords).sum(),
                tableReports.stream().mapToInt(TableOperationReport::sourceRows).sum(),
                tableReports.stream().mapToInt(TableOperationReport::targetRows).sum(),
                tableReports);
    }
}
