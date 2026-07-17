package br.com.escola.responsiblesservice.application.state;

import java.util.List;

public record LeituraModeloSyncSummary(
        boolean backfillEnabled,
        boolean reconciliationEnabled,
        String status,
        String reason,
        int batchSize,
        int plannedTables,
        int successfulTables,
        int backfilledRecords,
        int divergentRecords,
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
            boolean reconciliationPlanned,
            boolean idempotent,
            int sourceRows,
            int targetRows,
            int backfilledRecords,
            int divergentRecords) {
    }
}

