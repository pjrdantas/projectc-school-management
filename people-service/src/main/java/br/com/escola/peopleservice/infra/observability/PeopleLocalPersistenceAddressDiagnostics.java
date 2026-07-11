package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleAddressScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteAuthorityPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteMonolithAdapterPlan;
import br.com.escola.peopleservice.application.service.PeopleAddressScopeClosurePlanner;
import br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner;
import br.com.escola.peopleservice.application.service.PeopleAddressWriteMonolithAdapterPlanner;

final class PeopleLocalPersistenceAddressDiagnostics {

    private static final String ADDRESS_WRITE_SERVICE = "PeopleAddressWriteFallbackService";

    private final PeopleAddressWriteAuthorityPlanner addressWriteAuthorityPlanner;
    private final PeopleAddressWriteMonolithAdapterPlanner addressWriteMonolithAdapterPlanner =
            new PeopleAddressWriteMonolithAdapterPlanner();
    private final PeopleAddressScopeClosurePlanner addressScopeClosurePlanner =
            new PeopleAddressScopeClosurePlanner();

    PeopleLocalPersistenceAddressDiagnostics(PeopleAddressWriteAuthorityPlanner addressWriteAuthorityPlanner) {
        this.addressWriteAuthorityPlanner = addressWriteAuthorityPlanner;
    }

    Map<String, Object> diagnosticoAutoridadeEscritaEndereco() {
        PeopleAddressWriteAuthorityPlan plan = addressWriteAuthorityPlanner.planejarAutoridadeEscritaEndereco();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("localReadPrerequisiteClosed", plan.localReadPrerequisiteClosed());
        details.put("preparedCommandArtifacts", Map.of(
                "port", "PeopleAddressWritePort",
                "writeCommand", "PessoaEnderecoWriteCommand",
                "cleanupCommand", "PessoaEnderecoCleanupCommand",
                "result", "PessoaEnderecoWriteResult",
                "shadowService", ADDRESS_WRITE_SERVICE,
                "adapterCreated", true,
                "routeCreated", false,
                "localPersistenceConnected", false));
        details.put("shadowCommandExecution", Map.of(
                "service", ADDRESS_WRITE_SERVICE,
                "metric", "people.shadow.local.persistence.address.write.shadow.commands",
                "selectedSource", "monolith_proxy",
                "persistedLocally", false,
                "fallbackRequired", true,
                "localWriteEnabled", false));
        details.put("candidateOperations", plan.candidateOperations());
        details.put("monolithWriteAuthorities", plan.monolithWriteAuthorities());
        details.put("requiredContracts", plan.requiredContracts());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    Map<String, Object> diagnosticoAdapterEscritaMonolito() {
        PeopleAddressWriteMonolithAdapterPlan plan =
                addressWriteMonolithAdapterPlanner.planejarAdapterEscritaMonolito();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("monolithHttpWriteContractAvailable", plan.monolithHttpWriteContractAvailable());
        details.put("adapterImplementationAllowedNow", plan.adapterImplementationAllowedNow());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("localPersistenceAllowedNow", plan.localPersistenceAllowedNow());
        details.put("candidateOperations", plan.candidateOperations());
        details.put("requiredMonolithContracts", plan.requiredMonolithContracts());
        details.put("guardPreconditions", plan.guardPreconditions());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentPeopleServiceState", Map.of(
                "shadowService", ADDRESS_WRITE_SERVICE,
                "writePort", "PeopleAddressWritePort",
                "monolithWriteClientCreated", true,
                "monolithWriteClient", "MonolithPessoaAddressWriteClient",
                "monolithWriteClientEnabledByDefault", false,
                "guardProperty", "people.shadow.monolith.address-write-adapter-enabled",
                "localPersistenceConnected", false,
                "routeCreated", false));
        return details;
    }

    Map<String, Object> diagnosticoFechamentoEscopoPessoaEndereco() {
        PeopleAddressScopeClosurePlan plan =
                addressScopeClosurePlanner.planejarFechamentoEscopoPessoaEndereco();
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
                "keepAddressGuardDisabled", true,
                "keepAddressWritesOnMonolith", true,
                "nextPreferredFamily", "pessoa_documento",
                "reopenAddressInThisPhase", false));
        return details;
    }
}
