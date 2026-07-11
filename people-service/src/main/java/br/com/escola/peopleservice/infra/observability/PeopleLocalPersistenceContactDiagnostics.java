package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleContactInternalUsageCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleContactLocalReadPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleContactScopeClosurePlan;
import br.com.escola.peopleservice.application.service.PeopleContactInternalUsageCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleContactLocalReadPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleContactScopeClosurePlanner;

final class PeopleLocalPersistenceContactDiagnostics {

    private final PeopleContactLocalReadPreparationPlanner contactLocalReadPreparationPlanner =
            new PeopleContactLocalReadPreparationPlanner();
    private final PeopleContactInternalUsageCandidatePlanner contactInternalUsageCandidatePlanner =
            new PeopleContactInternalUsageCandidatePlanner();
    private final PeopleContactScopeClosurePlanner contactScopeClosurePlanner =
            new PeopleContactScopeClosurePlanner();

    Map<String, Object> diagnosticoPreparacaoLeituraLocalContato() {
        PeopleContactLocalReadPreparationPlan plan =
                contactLocalReadPreparationPlanner.planejarPreparacaoDeContatoLocal();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("contractPrepared", plan.contractPrepared());
        details.put("internalServicePrepared", plan.internalServicePrepared());
        details.put("adapterCreated", plan.adapterCreated());
        details.put("localPersistenceConnected", plan.localPersistenceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("bffFrontendChangeAllowedNow", plan.bffFrontendChangeAllowedNow());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("minimalInternalPayload", plan.minimalInternalPayload());
        details.put("firstConsumers", plan.firstConsumers());
        details.put("guardrails", plan.guardrails());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedArtifacts", Map.of(
                "port", "PeopleContactLocalReadPort",
                "response", "PessoaContatoLocalReadResponse",
                "adapter", "JdbcPeopleContactLocalReadAdapter",
                "internalService", "PeopleContactLocalReadService",
                "routeCreated", false,
                "adapterCreated", true,
                "localPersistenceConnected", true));
        details.put("currentRecommendation", Map.of(
                "keepContactReadInternalOnly", true,
                "keepMonolithAsWriteAuthority", true,
                "connectRealConsumerNow", false,
                "nextPreferredFamily", "people_contact_internal_consumer_diagnostic"));
        return details;
    }

    Map<String, Object> diagnosticoUsoInternoMinimoContato() {
        PeopleContactInternalUsageCandidatePlan plan =
                contactInternalUsageCandidatePlanner.planejarUsoInternoMinimo();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("internalUsageCandidateFound", plan.internalUsageCandidateFound());
        details.put("safeToConnectNow", plan.safeToConnectNow());
        details.put("externalRouteChangeRequired", plan.externalRouteChangeRequired());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("currentBlockers", plan.currentBlockers());
        details.put("preservedBoundaries", plan.preservedBoundaries());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    Map<String, Object> diagnosticoFechamentoEscopoContato() {
        PeopleContactScopeClosurePlan plan = contactScopeClosurePlanner.planejarFechamentoEscopoContato();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("readScopeClosed", plan.readScopeClosed());
        details.put("writeScopePreparedWithoutCutover", plan.writeScopePreparedWithoutCutover());
        details.put("activationRequiredNow", plan.activationRequiredNow());
        details.put("safeToStartNextFamilyDiagnostic", plan.safeToStartNextFamilyDiagnostic());
        details.put("closedCapabilities", plan.closedCapabilities());
        details.put("remainingActivationBlockers", plan.remainingActivationBlockers());
        details.put("nextFamilyCandidates", plan.nextFamilyCandidates());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "keepContactPreparedButUnused", true,
                "keepContactWriteAuthorityOnMonolith", true,
                "nextPreferredFamily", "next_backend_family",
                "reopenContactInThisPhase", false));
        return details;
    }
}
