package br.com.escola.professor.adapter.out.internal;

import java.net.URI;
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
        String resolvedBaseUrl = baseUrlResolvida();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", enabled);
        details.put("fallbackLocalOnError", fallbackLocalOnError);
        details.put("internalEndpointPrefix", INTERNAL_ENDPOINT_PREFIX);
        details.put("requestsTotal", totalContador("professor.internal.client.requests"));
        details.put("fallbacksTotal", totalContador("professor.internal.client.fallbacks"));

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

    private double valorContador(Meter meter) {
        for (Measurement measurement : meter.measure()) {
            if (measurement.getStatistic() == Statistic.COUNT) {
                return measurement.getValue();
            }
        }
        return 0.0d;
    }
}
