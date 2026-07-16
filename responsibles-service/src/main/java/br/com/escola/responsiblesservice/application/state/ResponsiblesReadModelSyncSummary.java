package br.com.escola.responsiblesservice.application.state;

import java.util.List;

public record ResponsiblesReadModelSyncSummary(
        boolean backfillEnabled,
        String status,
        String reason,
        int batchSize,
        int plannedTables,
        int successfulTables,
        int backfilledRecords,
        int sourceRows,
        int targetRows,
        List<TableOperationReport> tables) {

    public record TableOperationReport(
            String table,
            String keyColumn,
            String source,
            String target,
            String status,
            String reason,
            boolean backfillPlanned,
            boolean idempotent,
            int sourceRows,
            int targetRows,
            int backfilledRecords) {
    }
}
