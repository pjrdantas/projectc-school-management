package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentMetadataSchemaDiagnosticPlannerTest {

    @Test
    void devePlanejarSchemaMinimoSemLiberarAdapterLocal() {
        PeopleDocumentMetadataSchemaDiagnosticPlanner planner = new PeopleDocumentMetadataSchemaDiagnosticPlanner();

        var plan = planner.planejarSchemaMinimoDeMetadados();

        assertThat(plan.phase()).isEqualTo("Fase 74");
        assertThat(plan.slice()).isEqualTo("people_document_local_metadata_schema_diagnostic");
        assertThat(plan.status()).isEqualTo("schema_backfill_reconciliation_diagnosed_adapter_still_blocked");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_74_and_plan_people_document_local_adapter_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_local_metadata_adapter_preparation");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.localReadAdapterAllowedNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.reconciliationKey()).isEqualTo("people_documento_read_model.id_pessoa_documento");
    }
}
