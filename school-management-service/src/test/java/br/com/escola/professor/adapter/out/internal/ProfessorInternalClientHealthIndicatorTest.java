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
                .containsEntry("fallbackLocalOnError", true)
                .containsEntry("buscarPorIdCutoverEnabled", false)
                .containsEntry("listarCutoverEnabled", false)
                .containsEntry("listarAlocacoesCutoverEnabled", false)
                .containsEntry("listarPorTurmaCutoverEnabled", false)
                .containsEntry("listarFuncionariosElegiveisCutoverEnabled", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        assertThat(shadowReadRoutes).containsKeys(
                "criar",
                "vincularTurmaDisciplina",
                "listar",
                "buscarPorId",
                "listarAlocacoes",
                "listarPorTurma",
                "listarFuncionariosElegiveis");
    }

    @Test
    void deveReportarUpQuandoClienteInternoEstaHabilitadoComBaseUrlValidaEMetricas() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("professor.internal-client.enabled", "true")
                .withProperty("professor.internal-client.fallback-local-on-error", "true")
                .withProperty("professor.internal-client.buscar-por-id-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-alocacoes-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-por-turma-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-funcionarios-elegiveis-cutover-enabled", "true")
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
        Counter.builder("professor.internal.client.requests")
                .tag("operacao", "listarFuncionariosElegiveis")
                .tag("destino", "internal")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment();

        ProfessorInternalClientHealthIndicator indicator =
                new ProfessorInternalClientHealthIndicator(environment, meterRegistry);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", true)
                .containsEntry("fallbackLocalOnError", true)
                .containsEntry("buscarPorIdCutoverEnabled", true)
                .containsEntry("listarCutoverEnabled", true)
                .containsEntry("listarAlocacoesCutoverEnabled", true)
                .containsEntry("listarPorTurmaCutoverEnabled", true)
                .containsEntry("listarFuncionariosElegiveisCutoverEnabled", true)
                .containsEntry("baseUrlScheme", "http")
                .containsEntry("baseUrlHost", "localhost")
                .containsEntry("requestsTotal", 7.0d)
                .containsEntry("fallbacksTotal", 1.0d);
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
        Map<String, Object> listarAlocacoes = (Map<String, Object>) shadowReadRoutes.get("listarAlocacoes");
        @SuppressWarnings("unchecked")
        Map<String, Object> listarPorTurma = (Map<String, Object>) shadowReadRoutes.get("listarPorTurma");
        @SuppressWarnings("unchecked")
        Map<String, Object> listarFuncionariosElegiveis =
                (Map<String, Object>) shadowReadRoutes.get("listarFuncionariosElegiveis");
        assertThat(criar)
                .containsEntry("externalRoute", "POST /api/professores")
                .containsEntry("internalRoute", "POST /internal/professores")
                .containsEntry("internalSuccessTotal", 2.0d);
        assertThat(alocar)
                .containsEntry("externalRoute", "POST /api/professores/{id}/turmas-disciplinas")
                .containsEntry("internalRoute", "POST /internal/professores/{id}/turmas-disciplinas");
        assertThat(listar)
                .containsEntry("externalRoute", "GET /api/professores")
                .containsEntry("internalRoute", "GET /internal/professores")
                .containsEntry("internalSuccessTotal", 3.0d)
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property");
        assertThat(buscarPorId)
                .containsEntry("externalRoute", "GET /api/professores/{id}")
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property");
        assertThat(listarAlocacoes)
                .containsEntry("externalRoute", "GET /api/professores/{id}/turmas-disciplinas")
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property");
        assertThat(listarPorTurma)
                .containsEntry("externalRoute", "GET /api/turmas/{turmaId}/professores")
                .containsEntry("internalRoute", "GET /internal/professores/turmas/{turmaId}")
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property")
                .containsEntry("localFallbackTotal", 1.0d)
                .containsEntry("fallbacksTotal", 1.0d);
        assertThat(listarFuncionariosElegiveis)
                .containsEntry("externalRoute", "GET /api/professores/funcionarios-elegiveis")
                .containsEntry("internalRoute", "GET /internal/professores/funcionarios-elegiveis")
                .containsEntry("internalSuccessTotal", 1.0d)
                .containsEntry("fallbackStrategy", "disabled_for_route")
                .containsEntry("cutoverEnabled", true)
                .containsEntry("rollbackStrategy", "disable_property");
    }

    @Test
    void deveReportarOutOfServiceQuandoBaseUrlEhInvalida() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("professor.internal-client.enabled", "true")
                .withProperty("professor.internal-client.buscar-por-id-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-alocacoes-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-por-turma-cutover-enabled", "true")
                .withProperty("professor.internal-client.listar-funcionarios-elegiveis-cutover-enabled", "true")
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
