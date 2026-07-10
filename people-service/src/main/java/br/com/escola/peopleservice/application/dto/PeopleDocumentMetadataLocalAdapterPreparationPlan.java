package br.com.escola.peopleservice.application.dto;

import java.util.List;
import java.util.Map;

public record PeopleDocumentMetadataLocalAdapterPreparationPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean adapterImplementationAllowedNow,
        boolean adapterPrepared,
        boolean internalServiceConnected,
        boolean externalRouteCreated,
        boolean localReadCutoverAllowedNow,
        String candidateSource,
        String fallbackSource,
        String routingOperation,
        String schemaVersion,
        Map<String, Object> preparedArtifacts,
        List<String> blockerBeforeActivation,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
