package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentAlunoConsumerContractPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        String firstFutureConsumer,
        String consumerOperation,
        String candidateSource,
        String fallbackSource,
        boolean contractPrepared,
        boolean localReadServiceReusable,
        boolean safeToConnectNow,
        boolean routeChangeRequiredNow,
        boolean monolithChangeRequiredNow,
        boolean fallbackRequired,
        List<String> consumerInputKeys,
        List<String> consumerOutputExpectations,
        List<String> preservedBoundaries,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
