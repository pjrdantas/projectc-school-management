package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleProfessorScopeDiagnosticPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean diagnosticReadyNow,
        boolean internalContractSeparationAllowedNow,
        boolean localPersistenceAllowedNow,
        boolean externalRouteChangeAllowedNow,
        boolean fallbackToCurrentMonolithRequired,
        List<String> minimalReadCandidates,
        List<String> minimalWriteCandidates,
        List<String> monolithDependencies,
        List<String> consistencyImpacts,
        List<String> minimalMigrationRequirements,
        List<String> rollbackSteps,
        List<String> firstImplementationGuardrails,
        List<String> explicitlyOutOfScope) {
}
