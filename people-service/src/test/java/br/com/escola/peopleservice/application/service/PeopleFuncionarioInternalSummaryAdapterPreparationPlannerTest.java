package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioInternalSummaryAdapterPreparationPlannerTest {

    @Test
    void devePlanejarPreparacaoDoAdapterLocalSemRotaNemCutover() {
        PeopleFuncionarioInternalSummaryAdapterPreparationPlanner planner =
                new PeopleFuncionarioInternalSummaryAdapterPreparationPlanner();

        var plan = planner.planejarPreparacaoDoAdapterLocal();

        assertThat(plan.phase()).isEqualTo("Fase 81");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_adapter_preparation");
        assertThat(plan.status()).isEqualTo("jdbc_local_adapter_prepared_internal_fallback_only");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_81_and_plan_funcionario_internal_summary_backfill_reconciliation_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_backfill_reconciliation_preparation");
        assertThat(plan.adapterImplementationAllowedNow()).isTrue();
        assertThat(plan.adapterPrepared()).isTrue();
        assertThat(plan.internalServiceConnected()).isTrue();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_funcionario_read_model");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_internal_rh");
        assertThat(plan.schemaVersion()).isEqualTo("V6__create_people_funcionario_internal_summary_read_model.sql");
    }
}
