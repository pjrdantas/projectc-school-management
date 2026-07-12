package br.com.escola.peopleservice.infra.observability;

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
class PeopleReadModelHealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void deveExporSomenteEstadoOperacionalRealDaPersistenciaLocal() {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        Map<?, ?> health = client.get()
                .uri("/actuator/health/peopleReadModel")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
                .containsEntry("enabled", false)
                .containsEntry("localReadRoutingEnabled", false)
                .containsEntry("operationalMode", "monolith_only")
                .containsEntry("authoritativeWriteStorage", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> coreReadRoutes = (Map<String, Object>) details.get("coreReadRoutes");
        assertThat(coreReadRoutes).containsKeys(
                "listarTiposPessoa",
                "listarTiposEndereco",
                "buscarPorId",
                "consultarCadastro");

        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) coreReadRoutes.get("buscarPorId");
        assertThat(buscarPorId)
                .containsEntry("route", "GET /internal/v1/pessoas/{id}")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadEligible", false)
                .containsEntry("reason", "local-read-routing-disabled");

        @SuppressWarnings("unchecked")
        Map<String, Object> internalReadRoutes = (Map<String, Object>) details.get("internalReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) internalReadRoutes.get("address");
        assertThat(address)
                .containsEntry("route", "internal-operation:PessoaEnderecoPort")
                .containsEntry("selectedSource", "monolith_proxy");
    }
}
