package br.com.escola.documento.adapter.out.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.documento.application.service.DocumentoPeopleConsumerDiagnosticPlanner;

class DocumentoPeopleConsumerDiagnosticHealthIndicatorTest {

    @Test
    void deveExporDiagnosticoDoPrimeiroConsumidorDeDocumento() {
        DocumentoPeopleConsumerDiagnosticHealthIndicator indicator =
                new DocumentoPeopleConsumerDiagnosticHealthIndicator(new DocumentoPeopleConsumerDiagnosticPlanner());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("phase", "Fase 87")
                .containsEntry("slice", "documento_people_first_consumer_diagnostic")
                .containsEntry("status", "first_safe_consumer_is_documento_aluno_listar_por_aluno_without_route_change")
                .containsEntry("recommendedNextStep",
                        "prepare_documento_aluno_metadata_consumer_contract_without_changing_upload_or_delete")
                .containsEntry("minimalNextSlice", "documento_aluno_metadata_consumer_contract")
                .containsEntry("diagnosticReadyNow", true)
                .containsEntry("internalContractPreparationAllowedNow", true)
                .containsEntry("localPeopleConsumptionAllowedNow", false)
                .containsEntry("externalRouteChangeAllowedNow", false)
                .containsEntry("firstConsumerCandidate", "DocumentoAlunoService.listarPorAluno")
                .containsEntry("candidateExternalRoute", "GET /api/documentos-alunos/alunos/{alunoId}")
                .containsEntry("currentAuthority", "school-management-service:DocumentoGateway.findByEntidade")
                .containsEntry("candidateFutureSource",
                        "people-service:PeopleDocumentMetadataLocalReadPort.listarDocumentosPorPessoa");
        @SuppressWarnings("unchecked")
        Map<String, Object> currentRecommendation =
                (Map<String, Object>) health.getDetails().get("currentRecommendation");
        assertThat(currentRecommendation)
                .containsEntry("preferFirstImplementation", "DocumentoAlunoService.listarPorAluno")
                .containsEntry("keepUploadAndDeleteOnMonolith", true)
                .containsEntry("preparePeopleClientNow", false)
                .containsEntry("changeExternalRoutesNow", false);
    }
}
