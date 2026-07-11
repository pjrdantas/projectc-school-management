package br.com.escola.documento.adapter.out.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.health.show-details=always")
class DocumentoPeopleConsumerDiagnosticHealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void deveExporHealthDedicadoDoDiagnosticoDeDocumento() {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        Map<?, ?> health = client.get()
                .uri("/actuator/health/documentoPeopleConsumerDiagnostic")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
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
                .containsEntry("candidateExternalRoute", "GET /api/documentos-alunos/alunos/{alunoId}");

        Map<?, ?> readiness = client.get()
                .uri("/actuator/health")
                .retrieve()
                .body(Map.class);

        assertThat(readiness).isNotNull();
        assertThat(readiness.get("status")).isEqualTo("UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> readinessComponents = (Map<String, Object>) readiness.get("components");
        assertThat(readinessComponents).containsKey("documentoPeopleConsumerDiagnostic");
    }
}
