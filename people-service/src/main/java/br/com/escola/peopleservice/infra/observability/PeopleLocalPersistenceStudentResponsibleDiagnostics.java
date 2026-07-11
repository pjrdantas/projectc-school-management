package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleStudentResponsibleLinkScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleStudentResponsibleLinkScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.service.PeopleStudentResponsibleLinkScopeClosurePlanner;
import br.com.escola.peopleservice.application.service.PeopleStudentResponsibleLinkScopeDiagnosticPlanner;

final class PeopleLocalPersistenceStudentResponsibleDiagnostics {

    private final PeopleStudentResponsibleLinkScopeDiagnosticPlanner studentResponsibleLinkScopeDiagnosticPlanner =
            new PeopleStudentResponsibleLinkScopeDiagnosticPlanner();
    private final PeopleStudentResponsibleLinkScopeClosurePlanner studentResponsibleLinkScopeClosurePlanner =
            new PeopleStudentResponsibleLinkScopeClosurePlanner();

    Map<String, Object> diagnosticoEscopoVinculosAlunoResponsavel() {
        PeopleStudentResponsibleLinkScopeDiagnosticPlan plan =
                studentResponsibleLinkScopeDiagnosticPlanner.planejarDiagnosticoEscopoVinculosAlunoResponsavel();
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
                "preferFirstImplementation", "student_responsible_link_contract_closure",
                "reuseExistingAlunoAndResponsavelLookupServices", true,
                "keepParentescoAndStatusAlunoOutOfScopeNow", true,
                "prepareExternalRouteNow", false,
                "advanceToPersistenceNow", false));
        return details;
    }

    Map<String, Object> diagnosticoFechamentoEscopoVinculosAlunoResponsavel() {
        PeopleStudentResponsibleLinkScopeClosurePlan plan =
                studentResponsibleLinkScopeClosurePlanner.planejarFechamentoEscopoVinculosAlunoResponsavel();
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
                "keepStudentResponsibleLookupsPreparedButInternalOnly", true,
                "keepStudentResponsibleWritesOnMonolith", true,
                "nextPreferredFamily", "next_backend_family",
                "reopenStudentResponsibleInThisPhase", false));
        return details;
    }
}
