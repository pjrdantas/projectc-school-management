package br.com.escola.professor.adapter.out.internal;

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
                "professor.internal-client.enabled=true",
                "professor.internal-client.fallback-local-on-error=false",
                "professor.internal-client.buscar-por-id-cutover-enabled=true",
                "professor.internal-client.base-url=http://localhost:${local.server.port}"
        })
class ProfessorInternalClientHealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void deveExporHealthDedicadoEReadinessDoClienteInternoDeProfessor() {
        meterRegistry.counter(
                "professor.internal.client.requests",
                "operacao", "criar",
                "destino", "internal",
                "resultado", "success").increment();
        meterRegistry.counter(
                "professor.internal.client.requests",
                "operacao", "listar",
                "destino", "internal",
                "resultado", "success").increment(2.0d);
        meterRegistry.counter(
                "professor.internal.client.requests",
                "operacao", "listarPorTurma",
                "destino", "local",
                "resultado", "fallback").increment();
        meterRegistry.counter(
                "professor.internal.client.fallbacks",
                "operacao", "listarPorTurma",
                "causa", "RestClientException").increment();

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        Map<?, ?> health = client.get()
                .uri("/actuator/health/professorInternalClient")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
                .containsEntry("enabled", true)
                .containsEntry("fallbackLocalOnError", false)
                .containsEntry("buscarPorIdCutoverEnabled", true)
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 4.0d)
                .containsEntry("fallbacksTotal", 1.0d);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) details.get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> criar = (Map<String, Object>) shadowReadRoutes.get("criar");
        @SuppressWarnings("unchecked")
        Map<String, Object> alocar = (Map<String, Object>) shadowReadRoutes.get("vincularTurmaDisciplina");
        @SuppressWarnings("unchecked")
        Map<String, Object> listar = (Map<String, Object>) shadowReadRoutes.get("listar");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        @SuppressWarnings("unchecked")
        Map<String, Object> listarPorTurma = (Map<String, Object>) shadowReadRoutes.get("listarPorTurma");
        assertThat(criar)
                .containsEntry("externalRoute", "POST /api/professores")
                .containsEntry("internalSuccessTotal", 1.0d);
        assertThat(alocar)
                .containsEntry("externalRoute", "POST /api/professores/{id}/turmas-disciplinas");
        assertThat(listar)
                .containsEntry("externalRoute", "GET /api/professores")
                .containsEntry("internalSuccessTotal", 2.0d);
        assertThat(buscarPorId)
                .containsEntry("externalRoute", "GET /api/professores/{id}")
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property");
        assertThat(listarPorTurma)
                .containsEntry("externalRoute", "GET /api/turmas/{turmaId}/professores")
                .containsEntry("localFallbackTotal", 1.0d)
                .containsEntry("fallbacksTotal", 1.0d);

        Map<?, ?> readiness = client.get()
                .uri("/actuator/health")
                .retrieve()
                .body(Map.class);

        assertThat(readiness).isNotNull();
        assertThat(readiness.get("status")).isEqualTo("UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> readinessComponents = (Map<String, Object>) readiness.get("components");
        assertThat(readinessComponents).containsKey("professorInternalClient");
        @SuppressWarnings("unchecked")
        Map<String, Object> professorInternalClient =
                (Map<String, Object>) readinessComponents.get("professorInternalClient");
        assertThat(professorInternalClient.get("status")).isEqualTo("UP");
    }
}
