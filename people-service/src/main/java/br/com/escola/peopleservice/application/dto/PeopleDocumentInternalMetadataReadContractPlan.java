package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentInternalMetadataReadContractPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean contractPrepared,
        boolean internalServicePrepared,
        boolean adapterCreated,
        boolean localPersistenceConnected,
        boolean externalRouteCreated,
        boolean bffFrontendChangeAllowedNow,
        boolean writeCutoverAllowedNow,
        String candidateSource,
        String fallbackSource,
        boolean fallbackRequired,
        List<String> minimalInternalPayload,
        List<String> firstConsumers,
        List<String> guardrails,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
