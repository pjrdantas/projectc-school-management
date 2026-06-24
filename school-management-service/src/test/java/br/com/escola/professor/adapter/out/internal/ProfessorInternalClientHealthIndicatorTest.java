package br.com.escola.professor.adapter.out.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.mock.env.MockEnvironment;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ProfessorInternalClientHealthIndicatorTest {

    @Test
    void deveReportarUpQuandoClienteInternoEstaDesabilitado() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("professor.internal-client.enabled", "false");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        ProfessorInternalClientHealthIndicator indicator =
                new ProfessorInternalClientHealthIndicator(environment, meterRegistry);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", false)
                .containsEntry("mode", "disabled")
                .containsEntry("fallbackLocalOnError", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        assertThat(shadowReadRoutes).containsKeys("listar", "buscarPorId", "listarAlocacoes", "listarPorTurma");
    }

    @Test
    void deveReportarUpQuandoClienteInternoEstaHabilitadoComBaseUrlValidaEMetricas() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("professor.internal-client.enabled", "true")
                .withProperty("professor.internal-client.fallback-local-on-error", "false")
                .withProperty("professor.internal-client.base-url", "http://localhost:8080");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("professor.internal.client.requests")
                .tag("operacao", "criar")
                .tag("destino", "internal")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(2.0d);
        Counter.builder("professor.internal.client.requests")
                .tag("operacao", "listar")
                .tag("destino", "internal")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(3.0d);
        Counter.builder("professor.internal.client.requests")
                .tag("operacao", "listarPorTurma")
                .tag("destino", "local")
                .tag("resultado", "fallback")
                .register(meterRegistry)
                .increment();
        Counter.builder("professor.internal.client.fallbacks")
                .tag("operacao", "listarPorTurma")
                .tag("causa", "RestClientException")
                .register(meterRegistry)
                .increment();

        ProfessorInternalClientHealthIndicator indicator =
                new ProfessorInternalClientHealthIndicator(environment, meterRegistry);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", true)
                .containsEntry("fallbackLocalOnError", false)
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 6.0d)
                .containsEntry("fallbacksTotal", 1.0d);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> listar = (Map<String, Object>) shadowReadRoutes.get("listar");
        @SuppressWarnings("unchecked")
        Map<String, Object> listarPorTurma = (Map<String, Object>) shadowReadRoutes.get("listarPorTurma");
        assertThat(listar)
                .containsEntry("externalRoute", "GET /api/professores")
                .containsEntry("internalRoute", "GET /internal/professores")
                .containsEntry("internalSuccessTotal", 3.0d);
        assertThat(listarPorTurma)
                .containsEntry("externalRoute", "GET /api/turmas/{turmaId}/professores")
                .containsEntry("internalRoute", "GET /internal/professores/turmas/{turmaId}")
                .containsEntry("localFallbackTotal", 1.0d)
                .containsEntry("fallbacksTotal", 1.0d);
    }

    @Test
    void deveReportarOutOfServiceQuandoBaseUrlEhInvalida() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("professor.internal-client.enabled", "true")
                .withProperty("professor.internal-client.base-url", "://invalida");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        ProfessorInternalClientHealthIndicator indicator =
                new ProfessorInternalClientHealthIndicator(environment, meterRegistry);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("enabled", true)
                .containsEntry("mode", "enabled")
                .containsEntry("reason", "base-url-invalida")
                .containsEntry("resolvedBaseUrl", "://invalida");
    }
}
