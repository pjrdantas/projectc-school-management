package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentLocalReadCandidatePlannerTest {

    @Test
    void devePriorizarContinuidadeDePessoaDocumentoAntesDeFuncionario() {
        PeopleDocumentLocalReadCandidatePlanner planner = new PeopleDocumentLocalReadCandidatePlanner();

        var plan = planner.planejarCandidatoDeLeituraLocalDeDocumento();

        assertThat(plan.phase()).isEqualTo("Fase 74");
        assertThat(plan.slice()).isEqualTo("people_document_local_metadata_adapter_diagnostic");
        assertThat(plan.status())
                .isEqualTo("metadata_local_read_candidate_diagnostic_started_continue_document_family");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_people_document_local_metadata_schema_diagnostic_without_route_or_upload_migration");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_local_metadata_schema_diagnostic");
        assertThat(plan.schemaDiagnosticAllowedNow()).isTrue();
        assertThat(plan.adapterDiagnosticAllowedNow()).isTrue();
        assertThat(plan.continueWithDocumentFamilyNow()).isTrue();
        assertThat(plan.switchToFuncionarioNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.sourceTables()).containsExactly(
                "pessoa_documento",
                "documento",
                "tipo_documento",
                "pessoa");
    }
}
