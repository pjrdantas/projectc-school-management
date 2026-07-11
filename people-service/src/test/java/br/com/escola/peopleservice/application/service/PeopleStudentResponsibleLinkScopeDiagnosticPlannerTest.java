package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleStudentResponsibleLinkScopeDiagnosticPlannerTest {

    @Test
    void devePlanejarDiagnosticoDoEscopoDeVinculosAlunoResponsavel() {
        PeopleStudentResponsibleLinkScopeDiagnosticPlanner planner =
                new PeopleStudentResponsibleLinkScopeDiagnosticPlanner();

        var plan = planner.planejarDiagnosticoEscopoVinculosAlunoResponsavel();

        assertThat(plan.phase()).isEqualTo("Fase 96");
        assertThat(plan.slice()).isEqualTo("student_responsible_link_contract_diagnostic");
        assertThat(plan.status())
                .isEqualTo("student_responsible_person_link_contracts_exist_without_explicit_family_closure");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_minimal_student_responsible_link_contract_closure_without_new_route_or_persistence");
        assertThat(plan.minimalNextSlice()).isEqualTo("student_responsible_link_contract_closure");
        assertThat(plan.diagnosticReadyNow()).isTrue();
        assertThat(plan.internalContractSeparationAllowedNow()).isTrue();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.externalRouteChangeAllowedNow()).isFalse();
        assertThat(plan.fallbackToCurrentMonolithRequired()).isTrue();
        assertThat(plan.minimalReadCandidates()).contains(
                "resolver alunoId para pessoaId por escola",
                "resolver responsavelId para pessoaId por escola");
        assertThat(plan.monolithDependencies()).contains(
                "aluno e responsavel ainda dependem do cadastro base de pessoa no monolito",
                "parentesco e status_aluno permanecem sem contrato proprio no people-service");
    }
}
