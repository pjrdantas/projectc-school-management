package br.com.escola.documento.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentoPeopleConsumerDiagnosticPlannerTest {

    @Test
    void devePlanejarPrimeiroConsumidorDeMetadataPeople() {
        DocumentoPeopleConsumerDiagnosticPlanner planner = new DocumentoPeopleConsumerDiagnosticPlanner();

        var plan = planner.planejarPrimeiroConsumidorDeMetadataPeople();

        assertThat(plan.phase()).isEqualTo("Fase 87");
        assertThat(plan.slice()).isEqualTo("documento_people_first_consumer_diagnostic");
        assertThat(plan.status()).isEqualTo("first_safe_consumer_is_documento_aluno_listar_por_aluno_without_route_change");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_documento_aluno_metadata_consumer_contract_without_changing_upload_or_delete");
        assertThat(plan.minimalNextSlice()).isEqualTo("documento_aluno_metadata_consumer_contract");
        assertThat(plan.diagnosticReadyNow()).isTrue();
        assertThat(plan.internalContractPreparationAllowedNow()).isTrue();
        assertThat(plan.localPeopleConsumptionAllowedNow()).isFalse();
        assertThat(plan.externalRouteChangeAllowedNow()).isFalse();
        assertThat(plan.firstConsumerCandidate()).isEqualTo("DocumentoAlunoService.listarPorAluno");
        assertThat(plan.candidateFutureSource())
                .isEqualTo("people-service:PeopleDocumentMetadataLocalReadPort.listarDocumentosPorPessoa");
    }
}
