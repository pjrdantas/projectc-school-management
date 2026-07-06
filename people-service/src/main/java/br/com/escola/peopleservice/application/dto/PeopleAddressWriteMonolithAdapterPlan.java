package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleAddressWriteMonolithAdapterPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean monolithHttpWriteContractAvailable,
        boolean adapterImplementationAllowedNow,
        boolean writeCutoverAllowedNow,
        boolean localPersistenceAllowedNow,
        List<AdapterCandidateOperation> candidateOperations,
        List<String> requiredMonolithContracts,
        List<String> guardPreconditions,
        List<String> consistencyBlockers,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {

    public record AdapterCandidateOperation(
            String operation,
            String shadowCommand,
            String requiredMonolithRoute,
            String currentMonolithAuthority,
            boolean adapterAllowedNow,
            String reason) {
    }
}
