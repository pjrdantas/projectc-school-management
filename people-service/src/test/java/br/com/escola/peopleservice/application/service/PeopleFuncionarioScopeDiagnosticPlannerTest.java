package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioScopeDiagnosticPlannerTest {

    @Test
    void devePlanejarDiagnosticoDeEscopoFuncionario() {
        var planner = new PeopleFuncionarioScopeDiagnosticPlanner();

        var plan = planner.planejarDiagnosticoEscopoFuncionario();

        assertThat(plan.phase()).isEqualTo("Fase 79");
        assertThat(plan.slice()).isEqualTo("funcionario_contract_diagnostic");
        assertThat(plan.status())
                .isEqualTo("employee_summary_contract_preferred_professor_and_auth_dependencies_preserved_on_monolith");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_minimal_funcionario_internal_summary_contract_without_route_or_persistence_cutover");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_contract");
        assertThat(plan.diagnosticReadyNow()).isTrue();
        assertThat(plan.internalContractSeparationAllowedNow()).isTrue();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.externalRouteChangeAllowedNow()).isFalse();
        assertThat(plan.fallbackToCurrentMonolithRequired()).isTrue();
    }
}
