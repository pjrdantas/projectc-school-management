package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleTransactionalReadModelExpansionPlan(
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean migrationAllowedNow,
        boolean backfillAllowedNow,
        boolean localReadCutoverAllowedNow,
        List<TableExpansionDecision> candidateTables,
        List<String> requiredHardening,
        List<String> blockedTables,
        List<String> rollbackSteps) {

    public record TableExpansionDecision(
            String table,
            String primaryKey,
            List<String> columns,
            List<String> dependencies,
            List<String> supportedOperations,
            boolean includeInNextSlice,
            boolean migrationAllowed,
            boolean backfillAllowed,
            boolean localReadAllowed,
            String reason) {
    }
}
