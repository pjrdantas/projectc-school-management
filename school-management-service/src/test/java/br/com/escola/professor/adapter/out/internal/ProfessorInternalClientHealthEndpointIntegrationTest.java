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
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 1.0d)
                .containsEntry("fallbacksTotal", 0.0d);

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
