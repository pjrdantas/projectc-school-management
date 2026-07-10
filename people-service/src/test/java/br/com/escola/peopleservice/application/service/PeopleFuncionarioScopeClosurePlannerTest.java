package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioScopeClosurePlannerTest {

    @Test
    void devePlanejarFechamentoDoEscopoFuncionario() {
        PeopleFuncionarioScopeClosurePlanner planner = new PeopleFuncionarioScopeClosurePlanner();

        var plan = planner.planejarFechamentoEscopoFuncionario();

        assertThat(plan.phase()).isEqualTo("Fase 85");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_scope_closure_review");
        assertThat(plan.status()).isEqualTo("funcionario_internal_summary_scope_review_closed_ready_for_next_family_diagnostic");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("start_next_backend_family_without_reopening_funcionario_internal_summary");
        assertThat(plan.minimalNextSlice()).isEqualTo("next_backend_family_diagnostic");
        assertThat(plan.readScopeClosed()).isTrue();
        assertThat(plan.writeScopePreparedWithoutCutover()).isFalse();
        assertThat(plan.activationRequiredNow()).isFalse();
        assertThat(plan.safeToStartNextFamilyDiagnostic()).isTrue();
        assertThat(plan.closedCapabilities()).contains(
                "JDBC adapter and read model migration prepared for people_funcionario_read_model",
                "internal usage diagnostic completed without forcing an artificial consumer");
        assertThat(plan.remainingActivationBlockers()).contains(
                "no justified internal consumer exists in people-service for funcionario summary at this stage",
                "RH authority and auth dependencies remain on monolith");
        assertThat(plan.nextFamilyCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.family()).isEqualTo("next_backend_family");
            assertThat(candidate.status()).isEqualTo("allowed_now");
            assertThat(candidate.allowedNow()).isTrue();
        });
    }
}
