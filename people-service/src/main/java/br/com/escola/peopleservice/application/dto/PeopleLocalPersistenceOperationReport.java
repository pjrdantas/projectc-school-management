package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleLocalPersistenceOperationReport(
        boolean backfillEnabled,
        boolean reconciliationEnabled,
        String status,
        String reason,
        int batchSize,
        int plannedTables,
        boolean writesEnabled,
        boolean cutoverEnabled,
        List<TableOperationReport> tables) {

    public record TableOperationReport(
            String table,
            String keyColumn,
            String source,
            String target,
            boolean backfillPlanned,
            boolean reconciliationPlanned,
            boolean idempotent) {
    }
}
