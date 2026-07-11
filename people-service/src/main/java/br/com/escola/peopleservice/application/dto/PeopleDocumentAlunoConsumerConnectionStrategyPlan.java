package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleDocumentAlunoConsumerConnectionStrategyPlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean alunoPessoaLookupContractPrepared,
        boolean alunoPessoaLookupAdapterPrepared,
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
