package br.com.escola.professorservice.infra.observability;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.MonolithProfessorClientProperties;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowMonolith")
public class ProfessorShadowMonolithHealthIndicator implements HealthIndicator {

    private static final List<RouteMetricDescriptor> SHADOW_ROUTES = List.of(
            new RouteMetricDescriptor("listar", "GET /internal/v1/professores", "GET /internal/professores"),
            new RouteMetricDescriptor("buscarPorId", "GET /internal/v1/professores/{id}", "GET /internal/professores/{id}"),
            new RouteMetricDescriptor(
                    "listarAlocacoes",
                    "GET /internal/v1/professores/{id}/turmas-disciplinas",
                    "GET /internal/professores/{id}/turmas-disciplinas"),
            new RouteMetricDescriptor(
                    "listarPorTurma",
                    "GET /internal/v1/turmas/{turmaId}/professores",
                    "GET /internal/professores/turmas/{turmaId}"),
            new RouteMetricDescriptor(
                    "listarFuncionariosElegiveis",
                    "GET /internal/v1/professores/funcionarios-elegiveis",
                    "GET /internal/funcionarios/professor-elegiveis"));

    private final MonolithProfessorClientProperties properties;
    private final MeterRegistry meterRegistry;

    public ProfessorShadowMonolithHealthIndicator(
            MonolithProfessorClientProperties properties,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("dependency", "monolith");
        details.put("requestsTotal", totalContador("professor.shadow.monolith.requests"));
        details.put("failuresTotal", totalContador("professor.shadow.monolith.failures"));
        details.put("shadowReadRoutes", diagnosticoRotasShadow());

        try {
            URI uri = properties.baseUrl();
            if (uri == null || uri.getScheme() == null || uri.getHost() == null) {
                return foraDeServico(details, "base-url-invalida", String.valueOf(uri));
            }

            details.put("baseUrlScheme", uri.getScheme());
            details.put("baseUrlHost", uri.getHost());
            details.put("baseUrlPort", uri.getPort());
            details.put("connectTimeoutMs", properties.connectTimeout().toMillis());
            details.put("readTimeoutMs", properties.readTimeout().toMillis());
            return Health.up().withDetails(details).build();
        } catch (IllegalArgumentException exception) {
            return foraDeServico(details, "base-url-invalida", String.valueOf(properties.baseUrl()));
        }
    }

    private Health foraDeServico(Map<String, Object> details, String reason, String resolvedBaseUrl) {
        details.put("reason", reason);
        details.put("resolvedBaseUrl", resolvedBaseUrl);
        return Health.outOfService().withDetails(details).build();
    }

    private Map<String, Object> diagnosticoRotasShadow() {
        Map<String, Object> rotas = new LinkedHashMap<>();
        for (RouteMetricDescriptor descriptor : SHADOW_ROUTES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("shadowRoute", descriptor.shadowRoute());
            detalhe.put("monolithRoute", descriptor.monolithRoute());
            detalhe.put("monolithSuccessTotal", totalRequests(descriptor.operation(), "success"));
            detalhe.put("monolithNotFoundTotal", totalRequests(descriptor.operation(), "not_found"));
            detalhe.put("monolithErrorTotal", totalRequests(descriptor.operation(), "error"));
            detalhe.put("failuresTotal", totalFailures(descriptor.operation()));
            rotas.put(descriptor.operation(), detalhe);
        }
        return rotas;
    }

    private double totalRequests(String operation, String resultado) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.monolith.requests".equals(meter.getId().getName()))
                .filter(meter -> tagEquals(meter, "operacao", operation))
                .filter(meter -> tagEquals(meter, "resultado", resultado))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalFailures(String operation) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.monolith.failures".equals(meter.getId().getName()))
                .filter(meter -> tagEquals(meter, "operacao", operation))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalContador(String meterName) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meterName.equals(meter.getId().getName()))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private boolean tagEquals(Meter meter, String tagName, String expectedValue) {
        String actual = meter.getId().getTag(tagName);
        return expectedValue.equals(actual);
    }

    private double valorContador(Meter meter) {
        for (Measurement measurement : meter.measure()) {
            if (measurement.getStatistic() == Statistic.COUNT) {
                return measurement.getValue();
            }
        }
        return 0.0d;
    }

    private record RouteMetricDescriptor(String operation, String shadowRoute, String monolithRoute) {
    }
}
