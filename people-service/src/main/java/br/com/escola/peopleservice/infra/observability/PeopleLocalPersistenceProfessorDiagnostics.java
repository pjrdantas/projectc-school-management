package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleProfessorInternalSummaryContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleProfessorScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.service.PeopleProfessorInternalSummaryContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleProfessorScopeDiagnosticPlanner;

final class PeopleLocalPersistenceProfessorDiagnostics {

    private final PeopleProfessorScopeDiagnosticPlanner professorScopeDiagnosticPlanner =
            new PeopleProfessorScopeDiagnosticPlanner();
    private final PeopleProfessorInternalSummaryContractPlanner professorInternalSummaryContractPlanner =
            new PeopleProfessorInternalSummaryContractPlanner();

    Map<String, Object> diagnosticoEscopoProfessor() {
        PeopleProfessorScopeDiagnosticPlan plan =
                professorScopeDiagnosticPlanner.planejarDiagnosticoEscopoProfessor();
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
                "preferFirstImplementation", "professor_internal_summary_read_only",
                "keepProfessorReadsOnMonolith", true,
                "keepAcademicAllocationOutOfPeopleService", true,
                "prepareExternalRouteNow", false,
                "advanceToPersistenceNow", false));
        return details;
    }

    Map<String, Object> diagnosticoContratoInternoResumoProfessor() {
        PeopleProfessorInternalSummaryContractPlan plan =
                professorInternalSummaryContractPlanner.planejarContratoInternoResumoProfessor();
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
                "port", "PeopleProfessorInternalSummaryPort",
                "response", "PessoaProfessorInternalSummaryResponse",
                "internalService", "PeopleProfessorInternalSummaryService",
                "adapterCreated", false,
                "routeCreated", false,
                "localPersistenceConnected", false));
        return details;
    }
}
