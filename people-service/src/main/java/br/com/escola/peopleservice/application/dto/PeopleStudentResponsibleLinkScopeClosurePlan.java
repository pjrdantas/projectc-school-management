package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleStudentResponsibleLinkScopeClosurePlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean readScopeClosed,
        boolean writeScopePreparedWithoutCutover,
        boolean activationRequiredNow,
        boolean safeToStartNextFamilyDiagnostic,
        List<String> closedCapabilities,
        List<String> remainingActivationBlockers,
        List<NextFamilyCandidate> nextFamilyCandidates,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {

    public record NextFamilyCandidate(
            String family,
            String status,
            boolean allowedNow,
            String reason) {
    }
}
