package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleProfessorInternalSummaryContractPlannerTest {

    @Test
    void devePlanejarContratoInternoMinimoSemAdapterNemRota() {
        PeopleProfessorInternalSummaryContractPlanner planner =
                new PeopleProfessorInternalSummaryContractPlanner();

        var plan = planner.planejarContratoInternoResumoProfessor();

        assertThat(plan.phase()).isEqualTo("Fase 95");
        assertThat(plan.slice()).isEqualTo("professor_internal_summary_contract");
        assertThat(plan.status()).isEqualTo("internal_contract_prepared_no_adapter_no_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_95_and_plan_professor_internal_summary_adapter_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("professor_internal_summary_adapter_preparation");
        assertThat(plan.contractPrepared()).isTrue();
        assertThat(plan.internalServicePrepared()).isTrue();
        assertThat(plan.adapterCreated()).isFalse();
        assertThat(plan.localPersistenceConnected()).isFalse();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_professor_read_model_candidate");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_internal_professor");
    }
}
