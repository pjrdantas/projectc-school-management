package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentResponsavelConsumerConnectionStrategyPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean responsavelPessoaLookupContractPrepared,
        boolean responsavelPessoaLookupAdapterPrepared,
        boolean localResolutionReadyForConnection,
        boolean realConsumerConnected,
        boolean routeChangeRequiredNow,
        boolean legacyChangeRequiredNow,
        boolean fallbackRequired,
        List<String> preparedArtifacts,
        List<String> resolutionFlow,
        List<String> guardrails,
        List<String> rollbackSteps,
        List<String> explicitlyOutOfScope) {
}
