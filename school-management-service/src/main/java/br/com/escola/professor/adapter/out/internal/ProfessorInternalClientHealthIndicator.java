package br.com.escola.professor.adapter.out.internal;

import java.net.URI;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorInternalClient")
public class ProfessorInternalClientHealthIndicator implements HealthIndicator {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String INTERNAL_ENDPOINT_PREFIX = "/internal/professores";
    private static final List<RouteMetricDescriptor> SHADOW_READ_ROUTES = List.of(
            new RouteMetricDescriptor("criar", "POST /api/professores", "POST /internal/professores"),
            new RouteMetricDescriptor(
                    "vincularTurmaDisciplina",
                    "POST /api/professores/{id}/turmas-disciplinas",
                    "POST /internal/professores/{id}/turmas-disciplinas"),
            new RouteMetricDescriptor("listar", "GET /api/professores", "GET /internal/professores"),
            new RouteMetricDescriptor("buscarPorId", "GET /api/professores/{id}", "GET /internal/professores/{id}"),
            new RouteMetricDescriptor(
                    "listarAlocacoes",
                    "GET /api/professores/{id}/turmas-disciplinas",
                    "GET /internal/professores/{id}/turmas-disciplinas"),
            new RouteMetricDescriptor(
                    "listarPorTurma",
                    "GET /api/turmas/{turmaId}/professores",
                    "GET /internal/professores/turmas/{turmaId}"));

    private final Environment environment;
    private final MeterRegistry meterRegistry;

    public ProfessorInternalClientHealthIndicator(Environment environment, MeterRegistry meterRegistry) {
        this.environment = environment;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        boolean enabled = environment.getProperty("professor.internal-client.enabled", Boolean.class, false);
        boolean fallbackLocalOnError = environment.getProperty(
                "professor.internal-client.fallback-local-on-error",
                Boolean.class,
                true);
        boolean buscarPorIdCutoverEnabled = environment.getProperty(
                "professor.internal-client.buscar-por-id-cutover-enabled",
                Boolean.class,
                false);
        boolean listarCutoverEnabled = environment.getProperty(
                "professor.internal-client.listar-cutover-enabled",
                Boolean.class,
                false);
        boolean listarAlocacoesCutoverEnabled = environment.getProperty(
                "professor.internal-client.listar-alocacoes-cutover-enabled",
                Boolean.class,
                false);
        String resolvedBaseUrl = baseUrlResolvida();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", enabled);
        details.put("fallbackLocalOnError", fallbackLocalOnError);
        details.put("buscarPorIdCutoverEnabled", buscarPorIdCutoverEnabled);
        details.put("listarCutoverEnabled", listarCutoverEnabled);
        details.put("listarAlocacoesCutoverEnabled", listarAlocacoesCutoverEnabled);
        details.put("internalEndpointPrefix", INTERNAL_ENDPOINT_PREFIX);
        details.put("requestsTotal", totalContador("professor.internal.client.requests"));
        details.put("fallbacksTotal", totalContador("professor.internal.client.fallbacks"));
        details.put("shadowReadRoutes", diagnosticoRotasShadow());

        if (!enabled) {
            details.put("mode", "disabled");
            return Health.up()
                    .withDetails(details)
                    .build();
        }

        try {
            URI uri = URI.create(resolvedBaseUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return foraDeServico(details, "base-url-invalida", resolvedBaseUrl);
            }

            details.put("baseUrlScheme", uri.getScheme());
            details.put("baseUrlHost", uri.getHost());
            details.put("baseUrlPort", uri.getPort());
            return Health.up()
                    .withDetails(details)
                    .build();
        } catch (IllegalArgumentException exception) {
            return foraDeServico(details, "base-url-invalida", resolvedBaseUrl);
        }
    }

    private Health foraDeServico(Map<String, Object> details, String reason, String resolvedBaseUrl) {
        details.put("mode", "enabled");
        details.put("reason", reason);
        details.put("resolvedBaseUrl", resolvedBaseUrl);
        return Health.outOfService()
                .withDetails(details)
                .build();
    }

    private String baseUrlResolvida() {
        String configured = environment.getProperty("professor.internal-client.base-url", DEFAULT_BASE_URL);
        return environment.resolvePlaceholders(configured).trim();
    }

    private double totalContador(String meterName) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meterName.equals(meter.getId().getName()))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private Map<String, Object> diagnosticoRotasShadow() {
        Map<String, Object> rotas = new LinkedHashMap<>();
        for (RouteMetricDescriptor descriptor : SHADOW_READ_ROUTES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("externalRoute", descriptor.externalRoute());
            detalhe.put("internalRoute", descriptor.internalRoute());
            detalhe.put("fallbackStrategy", fallbackStrategy(descriptor.operation()));
            detalhe.put("internalSuccessTotal", totalRequests(descriptor.operation(), "internal", "success"));
            detalhe.put("internalErrorTotal", totalRequests(descriptor.operation(), "internal", "error"));
            detalhe.put("localFallbackTotal", totalRequests(descriptor.operation(), "local", "fallback"));
            detalhe.put("featureDisabledLocalTotal", totalRequests(descriptor.operation(), "local", "feature_disabled"));
            detalhe.put("fallbacksTotal", totalFallbacks(descriptor.operation()));
            if ("listar".equals(descriptor.operation())) {
                detalhe.put("cutoverEnabled", listarCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
            } else if ("listarAlocacoes".equals(descriptor.operation())) {
                detalhe.put("cutoverEnabled", listarAlocacoesCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
            } else if ("buscarPorId".equals(descriptor.operation())) {
                detalhe.put("cutoverEnabled", buscarPorIdCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
            }
            rotas.put(descriptor.operation(), detalhe);
        }
        return rotas;
    }

    private String fallbackStrategy(String operation) {
        if ("listar".equals(operation) && listarCutoverEnabled()) {
            return "disabled_for_route";
        }
        if ("listarAlocacoes".equals(operation) && listarAlocacoesCutoverEnabled()) {
            return "disabled_for_route";
        }
        if ("buscarPorId".equals(operation) && buscarPorIdCutoverEnabled()) {
            return "disabled_for_route";
        }
        return environment.getProperty("professor.internal-client.fallback-local-on-error", Boolean.class, true)
                ? "local_on_error"
                : "disabled_globally";
    }

    private boolean listarCutoverEnabled() {
        return environment.getProperty("professor.internal-client.listar-cutover-enabled", Boolean.class, false);
    }

    private boolean listarAlocacoesCutoverEnabled() {
        return environment.getProperty("professor.internal-client.listar-alocacoes-cutover-enabled", Boolean.class, false);
    }

    private boolean buscarPorIdCutoverEnabled() {
        return environment.getProperty("professor.internal-client.buscar-por-id-cutover-enabled", Boolean.class, false);
    }

    private double totalRequests(String operation, String destino, String resultado) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.internal.client.requests".equals(meter.getId().getName()))
                .filter(meter -> tagEquals(meter, "operacao", operation))
                .filter(meter -> tagEquals(meter, "destino", destino))
                .filter(meter -> tagEquals(meter, "resultado", resultado))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalFallbacks(String operation) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.internal.client.fallbacks".equals(meter.getId().getName()))
                .filter(meter -> tagEquals(meter, "operacao", operation))
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

    private record RouteMetricDescriptor(String operation, String externalRoute, String internalRoute) {
    }
}
