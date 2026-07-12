package br.com.escola.documento.adapter.out.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.documento.application.dto.DocumentoPeopleConsumerDiagnosticPlan;
import br.com.escola.documento.application.service.DocumentoPeopleConsumerDiagnosticPlanner;

@Component("documentoPeopleConsumerDiagnostic")
public class DocumentoPeopleConsumerDiagnosticHealthIndicator implements HealthIndicator {

    private final DocumentoPeopleConsumerDiagnosticPlanner planner;

    public DocumentoPeopleConsumerDiagnosticHealthIndicator(DocumentoPeopleConsumerDiagnosticPlanner planner) {
        this.planner = planner;
    }

    @Override
    public Health health() {
        DocumentoPeopleConsumerDiagnosticPlan plan = planner.planejarPrimeiroConsumidorDeMetadataPeople();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("diagnosticReadyNow", plan.diagnosticReadyNow());
        details.put("internalContractPreparationAllowedNow", plan.internalContractPreparationAllowedNow());
        details.put("localPeopleConsumptionAllowedNow", plan.localPeopleConsumptionAllowedNow());
        details.put("externalRouteChangeAllowedNow", plan.externalRouteChangeAllowedNow());
        details.put("firstConsumerCandidate", plan.firstConsumerCandidate());
        details.put("candidateExternalRoute", plan.candidateExternalRoute());
        details.put("currentAuthority", plan.currentAuthority());
        details.put("candidateFutureSource", plan.candidateFutureSource());
        details.put("reasonsWhyMinimal", plan.reasonsWhyMinimal());
        details.put("currentDependencies", plan.currentDependencies());
        details.put("consistencyImpacts", plan.consistencyImpacts());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("preservedBoundaries", plan.preservedBoundaries());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "preferFirstImplementation", "DocumentoAlunoService.listarPorAluno",
                "keepUploadAndDeleteOnMonolith", true,
                "preparePeopleClientNow", false,
                "changeExternalRoutesNow", false));

        return Health.up()
                .withDetails(details)
                .build();
    }
}
