package br.com.escola.professorservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.endpoint.health.show-details=always",
                "professor.shadow.monolith.base-url=http://localhost:8080"
        })
class LegacyHealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void deveExporHealthDedicadoDoShadowComDiagnosticoApenasDasRotasLegadasResiduais() {
        meterRegistry.counter(
                "professor.shadow.monolith.requests",
                "operacao", "criar",
                "destino", "monolith",
                "resultado", "success").increment();
        meterRegistry.counter(
                "professor.shadow.monolith.requests",
                "operacao", "vincularTurmaDisciplina",
                "destino", "monolith",
                "resultado", "success").increment();
        meterRegistry.counter(
                "professor.shadow.monolith.requests",
                "operacao", "listar",
                "destino", "monolith",
                "resultado", "success").increment(2.0d);
        meterRegistry.counter(
                "professor.shadow.monolith.requests",
                "operacao", "listarFuncionariosElegiveis",
                "destino", "monolith",
                "resultado", "error").increment();
        meterRegistry.counter(
                "professor.shadow.monolith.failures",
                "operacao", "listarFuncionariosElegiveis",
                "causa", "ResourceAccessException").increment();

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        Map<?, ?> health = client.get()
                .uri("/actuator/health/professorShadowMonolith")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
                .containsEntry("dependency", "monolith")
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 3.0d)
                .containsEntry("failuresTotal", 1.0d);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowRoutes = (Map<String, Object>) details.get("shadowRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> criar = (Map<String, Object>) shadowRoutes.get("criar");
        @SuppressWarnings("unchecked")
        Map<String, Object> alocar = (Map<String, Object>) shadowRoutes.get("vincularTurmaDisciplina");
        @SuppressWarnings("unchecked")
        Map<String, Object> listar = (Map<String, Object>) shadowRoutes.get("listar");
        @SuppressWarnings("unchecked")
        Map<String, Object> elegiveis = (Map<String, Object>) shadowRoutes.get("listarFuncionariosElegiveis");
        assertThat(criar)
                .containsEntry("shadowRoute", "POST /internal/v1/professores")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(alocar)
                .containsEntry("shadowRoute", "POST /internal/v1/professores/{id}/turmas-disciplinas")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(listar).isNull();
        assertThat(elegiveis)
                .containsEntry("shadowRoute", "GET /internal/v1/professores/funcionarios-elegiveis")
                .containsEntry("monolithErrorTotal", 1.0d)
                .containsEntry("failuresTotal", 1.0d);
    }
}

