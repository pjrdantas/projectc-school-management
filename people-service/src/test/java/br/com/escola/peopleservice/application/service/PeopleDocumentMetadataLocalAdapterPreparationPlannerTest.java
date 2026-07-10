package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentMetadataLocalAdapterPreparationPlannerTest {

    @Test
    void devePlanejarPreparacaoDoAdapterLocalDeDocumentoSemAtivacao() {
        PeopleDocumentMetadataLocalAdapterPreparationPlanner planner =
                new PeopleDocumentMetadataLocalAdapterPreparationPlanner();

        var plan = planner.planejarPreparacaoDoAdapterLocal();

        assertThat(plan.phase()).isEqualTo("Fase 75");
        assertThat(plan.slice()).isEqualTo("people_document_local_metadata_adapter_preparation");
        assertThat(plan.status()).isEqualTo("jdbc_local_adapter_prepared_internal_fallback_only");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_75_and_plan_people_document_backfill_reconciliation_preparation");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_backfill_reconciliation_preparation");
        assertThat(plan.adapterImplementationAllowedNow()).isTrue();
        assertThat(plan.adapterPrepared()).isTrue();
        assertThat(plan.internalServiceConnected()).isTrue();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_documento_read_model");
        assertThat(plan.schemaVersion()).isEqualTo("V5__create_people_document_metadata_read_model.sql");
    }
}
