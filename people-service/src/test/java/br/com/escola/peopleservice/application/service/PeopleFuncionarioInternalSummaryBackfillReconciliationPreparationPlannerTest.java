package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlannerTest {

    @Test
    void devePlanejarBackfillEReconciliacaoSemAtivarLeituraLocal() {
        PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner planner =
                new PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner();

        var plan = planner.planejarBackfillReconciliacao();

        assertThat(plan.phase()).isEqualTo("Fase 82");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_backfill_reconciliation_preparation");
        assertThat(plan.status())
                .isEqualTo("funcionario_internal_summary_backfill_reconciliation_prepared_no_read_cutover");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_82_and_keep_funcionario_internal_summary_local_read_blocked_until_green");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_local_read_activation_eligibility");
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.reconciliationAllowedNow()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.target()).isEqualTo("people_funcionario_read_model");
        assertThat(plan.reconciliationKey()).isEqualTo("people_funcionario_read_model.id_funcionario");
    }
}
