package br.com.escola.peopleservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.peopleservice.infra.config.MonolithPeopleClientProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleShadowMonolithHealthIndicatorTest {

    @Test
    void deveReportarUpComDiagnosticoDetalhadoPorRota() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.shadow.monolith.requests")
                .tag("operacao", "listarTiposPessoa")
                .tag("destino", "monolith")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment();
        Counter.builder("people.shadow.monolith.requests")
                .tag("operacao", "listarTiposEndereco")
                .tag("destino", "monolith")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment();
        Counter.builder("people.shadow.monolith.requests")
                .tag("operacao", "buscarPorId")
                .tag("destino", "monolith")
                .tag("resultado", "not_found")
                .register(meterRegistry)
                .increment();
        Counter.builder("people.shadow.monolith.requests")
                .tag("operacao", "consultarCadastro")
                .tag("destino", "monolith")
                .tag("resultado", "error")
                .register(meterRegistry)
                .increment();
        Counter.builder("people.shadow.monolith.failures")
                .tag("operacao", "consultarCadastro")
                .tag("causa", "ResourceAccessException")
                .register(meterRegistry)
                .increment();

        PeopleShadowMonolithHealthIndicator indicator = new PeopleShadowMonolithHealthIndicator(
                new MonolithPeopleClientProperties(
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
                .containsEntry("requestsTotal", 4.0d)
                .containsEntry("failuresTotal", 1.0d);

        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> tiposPessoa = (Map<String, Object>) shadowReadRoutes.get("listarTiposPessoa");
        @SuppressWarnings("unchecked")
        Map<String, Object> tiposEndereco = (Map<String, Object>) shadowReadRoutes.get("listarTiposEndereco");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        @SuppressWarnings("unchecked")
        Map<String, Object> consulta = (Map<String, Object>) shadowReadRoutes.get("consultarCadastro");

        assertThat(tiposPessoa)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/catalogos/tipos-pessoa")
                .containsEntry("monolithRoute", "GET /internal/pessoas/catalogos/tipos-pessoa")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(tiposEndereco)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/catalogos/tipos-endereco")
                .containsEntry("monolithRoute", "GET /internal/pessoas/catalogos/tipos-endereco")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(buscarPorId)
                .containsEntry("monolithNotFoundTotal", 1.0d);
        assertThat(consulta)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/consulta-cadastral")
                .containsEntry("monolithErrorTotal", 1.0d)
                .containsEntry("failuresTotal", 1.0d);
    }

    @Test
    void deveReportarOutOfServiceQuandoBaseUrlForInvalida() {
        PeopleShadowMonolithHealthIndicator indicator = new PeopleShadowMonolithHealthIndicator(
                new MonolithPeopleClientProperties(
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
