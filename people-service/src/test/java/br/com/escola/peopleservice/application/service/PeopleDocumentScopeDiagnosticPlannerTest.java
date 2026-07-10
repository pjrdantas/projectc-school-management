package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentScopeDiagnosticPlannerTest {

    @Test
    void devePlanejarDiagnosticoMinimoDePessoaDocumentoSemReabrirEndereco() {
        PeopleDocumentScopeDiagnosticPlanner planner = new PeopleDocumentScopeDiagnosticPlanner();

        var plan = planner.planejarDiagnosticoEscopoPessoaDocumento();

        assertThat(plan.phase()).isEqualTo("Fase 73");
        assertThat(plan.slice()).isEqualTo("people_document_contract_diagnostic");
        assertThat(plan.status()).isEqualTo("document_metadata_read_contract_preferred_write_cleanup_stays_on_monolith");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_internal_people_document_metadata_read_contract_without_bff_or_write_cutover");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_internal_metadata_read_contract");
        assertThat(plan.diagnosticReadyNow()).isTrue();
        assertThat(plan.internalContractSeparationAllowedNow()).isTrue();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.externalRouteChangeAllowedNow()).isFalse();
        assertThat(plan.fallbackToCurrentMonolithRequired()).isTrue();
        assertThat(plan.minimalReadCandidates()).contains(
                "listar metadados de documentos por pessoa para aluno/responsavel sem mover upload");
        assertThat(plan.minimalWriteCandidates()).contains(
                "cleanup por exclusao de aluno/responsavel continua no monolito");
        assertThat(plan.monolithDependencies()).contains(
                "DocumentoPersistenceGateway.findByEntidade/findById/deleteByEntidade",
                "AlunoPersistenceGateway.deleteById e ResponsavelPersistenceGateway.deleteById");
        assertThat(plan.explicitlyOutOfScope()).contains(
                "binary-file-storage-migration",
                "reactivate-address-scope");
    }
}
