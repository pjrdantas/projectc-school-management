package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlannerTest {

    @Test
    void devePlanejarElegibilidadeDeAtivacaoInternaDeFuncionario() {
        var planner = new PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlanner();

        var plan = planner.planejarElegibilidadeDeAtivacao();

        assertThat(plan.phase()).isEqualTo("Fase 83");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_local_read_activation_eligibility");
        assertThat(plan.status()).isEqualTo("internal_funcionario_local_read_guarded_without_external_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_83_and_only_consider_funcionario_internal_usage_when_guard_is_green");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_internal_usage_candidate");
        assertThat(plan.internalServiceConnected()).isTrue();
        assertThat(plan.localReadGuardPrepared()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
        assertThat(plan.routingOperation()).isEqualTo("funcionarioInternalSummaryLocalRead");
        assertThat(plan.candidateSource()).isEqualTo("people_funcionario_read_model");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_internal_rh");
    }
}
