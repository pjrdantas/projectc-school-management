package br.com.escola.documento.application.dto;

import java.util.List;

public record DocumentoPeopleConsumerDiagnosticPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean diagnosticReadyNow,
        boolean internalContractPreparationAllowedNow,
        boolean localPeopleConsumptionAllowedNow,
        boolean externalRouteChangeAllowedNow,
        String firstConsumerCandidate,
        String candidateExternalRoute,
        String currentAuthority,
        String candidateFutureSource,
        List<String> reasonsWhyMinimal,
        List<String> currentDependencies,
        List<String> consistencyImpacts,
        List<String> rollbackSteps,
        List<String> preservedBoundaries,
        List<String> explicitlyOutOfScope) {
}
