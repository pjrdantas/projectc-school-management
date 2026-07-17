package br.com.escola.professorservice.infra.observability;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.LegacyClientProperties;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowMonolith")
public class LegacyHealthIndicator implements HealthIndicator {

    private static final List<RouteMetricDescriptor> SHADOW_ROUTES = List.of(
            new RouteMetricDescriptor("criar", "POST /internal/v1/professores", "POST /internal/professores"),
            new RouteMetricDescriptor(
                    "vincularTurmaDisciplina",
                    "POST /internal/v1/professores/{id}/turmas-disciplinas",
                    "POST /internal/professores/{id}/turmas-disciplinas"),
            new RouteMetricDescriptor(
                    "listarFuncionariosElegiveis",
                    "GET /internal/v1/professores/funcionarios-elegiveis",
                    "GET /internal/funcionarios/professor-elegiveis"));
    private static final Set<String> ACTIVE_OPERATIONS = SHADOW_ROUTES.stream()
            .map(RouteMetricDescriptor::operation)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final LegacyClientProperties properties;
    private final MeterRegistry meterRegistry;

    public LegacyHealthIndicator(
            LegacyClientProperties properties,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("dependency", "monolith");
        details.put("requestsTotal", totalRequestsAtivas());
        details.put("failuresTotal", totalFailuresAtivas());
        details.put("shadowRoutes", diagnosticoRotasShadow());

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
                .filter(meter -> ACTIVE_OPERATIONS.contains(meter.getId().getTag("operacao")))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalRequestsAtivas() {
        return totalContador("professor.shadow.monolith.requests");
    }

    private double totalFailuresAtivas() {
        return totalContador("professor.shadow.monolith.failures");
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

