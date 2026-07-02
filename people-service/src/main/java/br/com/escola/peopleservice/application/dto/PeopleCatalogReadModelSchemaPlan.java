package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleCatalogReadModelSchemaPlan(
        String status,
        String recommendedNextStep,
        boolean migrationAllowedNow,
        boolean physicalSchemaRequiredNext,
        boolean localReadAdapterRequiredNext,
        boolean readCutoverAllowed,
        boolean writeCutoverAllowed,
        List<TableSchemaDecision> tables,
        List<String> excludedTables,
        List<String> blockers,
        List<String> rollbackSteps) {

    public record TableSchemaDecision(
            String table,
            String primaryKey,
            String uniqueKey,
            List<String> columns,
            String seedSource,
            String operation,
            boolean includeInFirstMigration,
            String reason) {
    }
}
