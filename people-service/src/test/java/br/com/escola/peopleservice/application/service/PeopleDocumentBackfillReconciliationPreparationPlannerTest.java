package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentBackfillReconciliationPreparationPlannerTest {

    @Test
    void devePlanejarBackfillReconciliacaoDeDocumentoSemCutover() {
        PeopleDocumentBackfillReconciliationPreparationPlanner planner =
                new PeopleDocumentBackfillReconciliationPreparationPlanner();

        var plan = planner.planejarBackfillReconciliacao();

        assertThat(plan.phase()).isEqualTo("Fase 76");
        assertThat(plan.slice()).isEqualTo("people_document_backfill_reconciliation_preparation");
        assertThat(plan.status()).isEqualTo("document_metadata_backfill_reconciliation_prepared_no_read_cutover");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_76_and_keep_document_local_read_blocked_until_green");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_local_read_activation_eligibility");
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.reconciliationAllowedNow()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.target()).isEqualTo("people_documento_read_model");
    }
}
