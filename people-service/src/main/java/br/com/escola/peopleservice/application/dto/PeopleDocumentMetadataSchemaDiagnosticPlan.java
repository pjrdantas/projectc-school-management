package br.com.escola.peopleservice.application.dto;

import java.util.List;
import java.util.Map;

public record PeopleDocumentMetadataSchemaDiagnosticPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean migrationAllowedNow,
        boolean backfillAllowedNow,
        boolean localReadAdapterAllowedNow,
        boolean localReadCutoverAllowedNow,
        String reconciliationKey,
        String nextImplementationSlice,
        Map<String, List<String>> minimalColumns,
        Map<String, Object> schemaMigration,
        Map<String, Object> backfillReconciliation,
        String ownershipRule,
        String caminhoArquivoPolicy,
        List<String> secondaryReconciliationChecks,
        List<String> blockersBeforeAdapter,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
