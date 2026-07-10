package br.com.escola.peopleservice.application.dto;

import java.util.List;
import java.util.Map;

public record PeopleDocumentLocalReadCandidatePlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean schemaDiagnosticAllowedNow,
        boolean adapterDiagnosticAllowedNow,
        boolean continueWithDocumentFamilyNow,
        boolean switchToFuncionarioNow,
        boolean localReadCutoverAllowedNow,
        List<String> sourceTables,
        Map<String, List<String>> minimalCandidateColumns,
        String reconciliationKey,
        List<String> secondaryReconciliationChecks,
        List<String> ownershipRules,
        List<String> consistencyBlockers,
        List<String> migrationPrerequisites,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
