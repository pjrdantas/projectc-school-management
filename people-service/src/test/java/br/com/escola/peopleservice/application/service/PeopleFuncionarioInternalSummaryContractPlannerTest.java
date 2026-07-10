package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioInternalSummaryContractPlannerTest {

    @Test
    void devePlanejarContratoInternoMinimoSemAdapterNemRota() {
        PeopleFuncionarioInternalSummaryContractPlanner planner =
                new PeopleFuncionarioInternalSummaryContractPlanner();

        var plan = planner.planejarContratoInternoResumoFuncionario();

        assertThat(plan.phase()).isEqualTo("Fase 80");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_contract");
        assertThat(plan.status()).isEqualTo("internal_contract_prepared_no_adapter_no_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_80_and_plan_funcionario_internal_summary_adapter_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_adapter_preparation");
        assertThat(plan.contractPrepared()).isTrue();
        assertThat(plan.internalServicePrepared()).isTrue();
        assertThat(plan.adapterCreated()).isFalse();
        assertThat(plan.localPersistenceConnected()).isFalse();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_funcionario_read_model_candidate");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_internal_rh");
    }
}
