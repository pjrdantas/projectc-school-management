package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleProfessorInternalSummaryAdapterPreparationPlannerTest {

    @Test
    void devePlanejarPreparacaoDoAdapterLocalSemRotaNemCutover() {
        PeopleProfessorInternalSummaryAdapterPreparationPlanner planner =
                new PeopleProfessorInternalSummaryAdapterPreparationPlanner();

        var plan = planner.planejarPreparacaoDoAdapterLocal();

        assertThat(plan.phase()).isEqualTo("Fase 95");
        assertThat(plan.slice()).isEqualTo("professor_internal_summary_adapter_preparation");
        assertThat(plan.status()).isEqualTo("adapter_preparation_diagnosed_contract_ready_keep_local_read_inactive");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_95_and_formally_finish_professor_initial_block");
        assertThat(plan.minimalNextSlice()).isEqualTo("professor_initial_block_closure");
        assertThat(plan.adapterImplementationAllowedNow()).isTrue();
        assertThat(plan.adapterPrepared()).isFalse();
        assertThat(plan.internalServiceConnected()).isTrue();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_professor_read_model_candidate");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_internal_professor");
        assertThat(plan.schemaVersion()).isEqualTo("not-created-in-this-phase");
    }
}
