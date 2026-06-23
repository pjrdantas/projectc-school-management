package br.com.escola.professor.adapter.out.internal;

import static org.assertj.core.api.Assertions.assertThat;

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
        Counter.builder("professor.internal.client.fallbacks")
                .tag("operacao", "buscarPorId")
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
                .containsEntry("requestsTotal", 2.0d)
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
