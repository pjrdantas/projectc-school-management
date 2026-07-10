package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentLocalReadActivationEligibilityPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean internalServiceConnected,
        boolean localReadGuardPrepared,
        boolean localReadCutoverAllowedNow,
        boolean externalRouteCreated,
        boolean fallbackRequired,
        String routingOperation,
        String candidateSource,
        String fallbackSource,
        List<String> guardPreconditions,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
