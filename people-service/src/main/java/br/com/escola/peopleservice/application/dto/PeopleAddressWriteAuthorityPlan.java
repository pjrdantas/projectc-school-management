package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleAddressWriteAuthorityPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean writeCutoverAllowedNow,
        boolean migrationAllowedNow,
        boolean backfillAllowedNow,
        boolean localReadPrerequisiteClosed,
        List<WriteAuthorityDecision> candidateOperations,
        List<String> monolithWriteAuthorities,
        List<String> requiredContracts,
        List<String> consistencyBlockers,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {

    public record WriteAuthorityDecision(
            String operation,
            String currentOwner,
            String candidateOwner,
            List<String> sourceConsumers,
            List<String> affectedTables,
            boolean allowedNow,
            String reason) {
    }
}
