package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentMetadataSchemaDiagnosticPlannerTest {

    @Test
    void devePlanejarSchemaMinimoComAdapterPreparadoSemAtivacao() {
        PeopleDocumentMetadataSchemaDiagnosticPlanner planner = new PeopleDocumentMetadataSchemaDiagnosticPlanner();

        var plan = planner.planejarSchemaMinimoDeMetadados();

        assertThat(plan.phase()).isEqualTo("Fase 75");
        assertThat(plan.slice()).isEqualTo("people_document_local_metadata_schema_diagnostic");
        assertThat(plan.status()).isEqualTo("schema_preserved_and_jdbc_adapter_prepared_still_not_activated");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_75_and_plan_people_document_backfill_reconciliation_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_backfill_reconciliation_preparation");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.localReadAdapterAllowedNow()).isTrue();
        assertThat(plan.localReadAdapterPrepared()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.reconciliationKey()).isEqualTo("people_documento_read_model.id_pessoa_documento");
    }
}
