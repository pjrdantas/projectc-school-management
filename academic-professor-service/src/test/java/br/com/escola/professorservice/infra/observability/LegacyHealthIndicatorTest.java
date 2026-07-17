package br.com.escola.professorservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.professorservice.infra.config.LegacyClientProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class LegacyHealthIndicatorTest {

    @Test
    void deveReportarUpComDiagnosticoDetalhadoPorRota() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("professor.shadow.monolith.requests")
                .tag("operacao", "criar")
                .tag("destino", "monolith")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment();
        Counter.builder("professor.shadow.monolith.requests")
                .tag("operacao", "vincularTurmaDisciplina")
                .tag("destino", "monolith")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment();
        Counter.builder("professor.shadow.monolith.requests")
                .tag("operacao", "listar")
                .tag("destino", "monolith")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(2.0d);
        Counter.builder("professor.shadow.monolith.requests")
                .tag("operacao", "buscarPorId")
                .tag("destino", "monolith")
                .tag("resultado", "not_found")
                .register(meterRegistry)
                .increment();
        Counter.builder("professor.shadow.monolith.requests")
                .tag("operacao", "listarPorTurma")
                .tag("destino", "monolith")
                .tag("resultado", "error")
                .register(meterRegistry)
                .increment();
        Counter.builder("professor.shadow.monolith.failures")
                .tag("operacao", "listarPorTurma")
                .tag("causa", "ResourceAccessException")
                .register(meterRegistry)
                .increment();

        LegacyHealthIndicator indicator = new LegacyHealthIndicator(
                new LegacyClientProperties(
                        java.net.URI.create("http://localhost:8080"),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5)),
                meterRegistry);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("dependency", "monolith")
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 6.0d)
                .containsEntry("failuresTotal", 1.0d);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
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
                .containsEntry("shadowRoute", "POST /internal/v1/professores")
                .containsEntry("monolithRoute", "POST /internal/professores")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(alocar)
                .containsEntry("shadowRoute", "POST /internal/v1/professores/{id}/turmas-disciplinas")
                .containsEntry("monolithRoute", "POST /internal/professores/{id}/turmas-disciplinas")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(listar)
                .containsEntry("shadowRoute", "GET /internal/v1/professores")
                .containsEntry("monolithRoute", "GET /internal/professores")
                .containsEntry("monolithSuccessTotal", 2.0d);
        assertThat(buscarPorId)
                .containsEntry("monolithNotFoundTotal", 1.0d);
        assertThat(listarPorTurma)
                .containsEntry("monolithErrorTotal", 1.0d)
                .containsEntry("failuresTotal", 1.0d);
    }

    @Test
    void deveReportarOutOfServiceQuandoBaseUrlForInvalida() {
        LegacyHealthIndicator indicator = new LegacyHealthIndicator(
                new LegacyClientProperties(
                        java.net.URI.create("http:///"),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5)),
                new SimpleMeterRegistry());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("dependency", "monolith")
                .containsEntry("reason", "base-url-invalida");
    }
}

