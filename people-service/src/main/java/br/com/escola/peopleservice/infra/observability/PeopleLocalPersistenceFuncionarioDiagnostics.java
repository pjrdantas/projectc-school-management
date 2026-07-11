package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryAdapterPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalUsageCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleLocalReadRoutingDecision;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryAdapterPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalUsageCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioScopeClosurePlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioScopeDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard;

final class PeopleLocalPersistenceFuncionarioDiagnostics {

    private final PeopleFuncionarioScopeDiagnosticPlanner funcionarioScopeDiagnosticPlanner =
            new PeopleFuncionarioScopeDiagnosticPlanner();
    private final PeopleFuncionarioInternalSummaryContractPlanner funcionarioInternalSummaryContractPlanner =
            new PeopleFuncionarioInternalSummaryContractPlanner();
    private final PeopleFuncionarioInternalSummaryAdapterPreparationPlanner funcionarioInternalSummaryAdapterPreparationPlanner =
            new PeopleFuncionarioInternalSummaryAdapterPreparationPlanner();
    private final PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner funcionarioInternalSummaryBackfillReconciliationPreparationPlanner =
            new PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner();
    private final PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlanner funcionarioInternalSummaryLocalReadActivationEligibilityPlanner =
            new PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlanner();
    private final PeopleFuncionarioInternalUsageCandidatePlanner funcionarioInternalUsageCandidatePlanner =
            new PeopleFuncionarioInternalUsageCandidatePlanner();
    private final PeopleFuncionarioScopeClosurePlanner funcionarioScopeClosurePlanner =
            new PeopleFuncionarioScopeClosurePlanner();

    private final PeopleLocalReadCutoverGuard readCutoverGuard;

    PeopleLocalPersistenceFuncionarioDiagnostics(PeopleLocalReadCutoverGuard readCutoverGuard) {
        this.readCutoverGuard = readCutoverGuard;
    }

    Map<String, Object> diagnosticoEscopoFuncionario() {
        PeopleFuncionarioScopeDiagnosticPlan plan =
                funcionarioScopeDiagnosticPlanner.planejarDiagnosticoEscopoFuncionario();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("diagnosticReadyNow", plan.diagnosticReadyNow());
        details.put("internalContractSeparationAllowedNow", plan.internalContractSeparationAllowedNow());
        details.put("localPersistenceAllowedNow", plan.localPersistenceAllowedNow());
        details.put("externalRouteChangeAllowedNow", plan.externalRouteChangeAllowedNow());
        details.put("fallbackToCurrentMonolithRequired", plan.fallbackToCurrentMonolithRequired());
        details.put("minimalReadCandidates", plan.minimalReadCandidates());
        details.put("minimalWriteCandidates", plan.minimalWriteCandidates());
        details.put("monolithDependencies", plan.monolithDependencies());
        details.put("consistencyImpacts", plan.consistencyImpacts());
        details.put("minimalMigrationRequirements", plan.minimalMigrationRequirements());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("firstImplementationGuardrails", plan.firstImplementationGuardrails());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "preferFirstImplementation", "funcionario_internal_summary_read_only",
                "keepProfessorAndAuthOnMonolith", true,
                "prepareExternalRouteNow", false,
                "advanceToPersistenceNow", false));
        return details;
    }

    Map<String, Object> diagnosticoContratoInternoResumoFuncionario() {
        PeopleFuncionarioInternalSummaryContractPlan plan =
                funcionarioInternalSummaryContractPlanner.planejarContratoInternoResumoFuncionario();
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
                "port", "PeopleFuncionarioInternalSummaryPort",
                "response", "PessoaFuncionarioInternalSummaryResponse",
                "internalService", "PeopleFuncionarioInternalSummaryService",
                "adapterCreated", false,
                "routeCreated", false,
                "localPersistenceConnected", false));
        return details;
    }

    Map<String, Object> diagnosticoPreparacaoAdapterResumoFuncionario() {
        PeopleFuncionarioInternalSummaryAdapterPreparationPlan plan =
                funcionarioInternalSummaryAdapterPreparationPlanner.planejarPreparacaoDoAdapterLocal();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("adapterImplementationAllowedNow", plan.adapterImplementationAllowedNow());
        details.put("adapterPrepared", plan.adapterPrepared());
        details.put("internalServiceConnected", plan.internalServiceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("routingOperation", plan.routingOperation());
        details.put("schemaVersion", plan.schemaVersion());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("blockerBeforeActivation", plan.blockerBeforeActivation());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    Map<String, Object> diagnosticoBackfillReconciliacaoResumoFuncionario() {
        PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlan plan =
                funcionarioInternalSummaryBackfillReconciliationPreparationPlanner.planejarBackfillReconciliacao();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("reconciliationAllowedNow", plan.reconciliationAllowedNow());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("source", plan.source());
        details.put("target", plan.target());
        details.put("reconciliationKey", plan.reconciliationKey());
        details.put("sourceTables", plan.sourceTables());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    Map<String, Object> diagnosticoElegibilidadeAtivacaoLeituraLocalResumoFuncionario() {
        PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlan plan =
                funcionarioInternalSummaryLocalReadActivationEligibilityPlanner.planejarElegibilidadeDeAtivacao();
        PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliarLeituraFuncionarioInternalSummary();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("internalServiceConnected", plan.internalServiceConnected());
        details.put("localReadGuardPrepared", plan.localReadGuardPrepared());
        details.put("localReadCutoverAllowedNow", decision.localReadEligible());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("routingOperation", decision.operation());
        details.put("shadowRoute", decision.shadowRoute());
        details.put("candidateSource", decision.candidateSource());
        details.put("selectedSource", decision.selectedSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("reason", decision.reason());
        details.put("guardPreconditions", plan.guardPreconditions());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    Map<String, Object> diagnosticoUsoInternoMinimoResumoFuncionario() {
        PeopleFuncionarioInternalUsageCandidatePlan plan =
                funcionarioInternalUsageCandidatePlanner.planejarUsoInternoMinimo();
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

    Map<String, Object> diagnosticoFechamentoEscopoFuncionario() {
        PeopleFuncionarioScopeClosurePlan plan =
                funcionarioScopeClosurePlanner.planejarFechamentoEscopoFuncionario();
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
                "keepFuncionarioGuardPreparedButUnused", true,
                "keepFuncionarioAuthorityOnMonolith", true,
                "nextPreferredFamily", "next_backend_family",
                "reopenFuncionarioInThisPhase", false));
        return details;
    }
}
