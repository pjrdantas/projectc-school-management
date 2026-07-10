package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentScopeClosurePlannerTest {

    @Test
    void devePlanejarFechamentoDoEscopoPessoaDocumento() {
        PeopleDocumentScopeClosurePlanner planner = new PeopleDocumentScopeClosurePlanner();

        var plan = planner.planejarFechamentoEscopoPessoaDocumento();

        assertThat(plan.phase()).isEqualTo("Fase 86");
        assertThat(plan.slice()).isEqualTo("people_document_metadata_scope_closure_review");
        assertThat(plan.status()).isEqualTo("people_document_metadata_scope_review_closed_ready_for_next_family_diagnostic");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("start_next_backend_family_without_reopening_people_document_metadata");
        assertThat(plan.minimalNextSlice()).isEqualTo("next_backend_family_diagnostic");
        assertThat(plan.readScopeClosed()).isTrue();
        assertThat(plan.writeScopePreparedWithoutCutover()).isFalse();
        assertThat(plan.activationRequiredNow()).isFalse();
        assertThat(plan.safeToStartNextFamilyDiagnostic()).isTrue();
        assertThat(plan.closedCapabilities()).contains(
                "local read candidate, schema and JDBC adapter prepared for people_documento_read_model",
                "internal usage diagnostic completed without forcing an artificial consumer");
        assertThat(plan.remainingActivationBlockers()).contains(
                "no justified internal consumer exists in people-service for document metadata at this stage",
                "upload, listagem and cleanup flows remain in school-management-service");
        assertThat(plan.nextFamilyCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.family()).isEqualTo("next_backend_family");
            assertThat(candidate.status()).isEqualTo("allowed_now");
            assertThat(candidate.allowedNow()).isTrue();
        });
    }
}
