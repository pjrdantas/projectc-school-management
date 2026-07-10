package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentBackfillReconciliationPreparationPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean migrationAllowedNow,
        boolean backfillAllowedNow,
        boolean reconciliationAllowedNow,
        boolean localReadCutoverAllowedNow,
        String source,
        String target,
        String reconciliationKey,
        List<String> sourceTables,
        List<String> consistencyBlockers,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
