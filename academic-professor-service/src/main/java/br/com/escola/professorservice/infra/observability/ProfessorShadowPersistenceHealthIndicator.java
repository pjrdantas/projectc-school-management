package br.com.escola.professorservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowPersistence")
public class ProfessorShadowPersistenceHealthIndicator implements HealthIndicator {

    private final ProfessorShadowLocalPersistenceProperties properties;
    private final ProfessorShadowJpaRepository repository;
    private final MeterRegistry meterRegistry;

    public ProfessorShadowPersistenceHealthIndicator(
            ProfessorShadowLocalPersistenceProperties properties,
            ProfessorShadowJpaRepository repository,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", properties.enabled());
        details.put("failOnError", properties.failOnError());
        details.put("requestsTotal", totalContador("professor.shadow.local.persistence.requests"));
        details.put("successTotal", totalRequests("criar", "success"));
        details.put("skippedDisabledTotal", totalRequests("criar", "skipped_disabled"));
        details.put("divergenceTotal", totalRequests("criar", "divergence"));
        details.put("errorTotal", totalRequests("criar", "error"));
        details.put("failuresTotal", totalContador("professor.shadow.local.persistence.failures"));

        try {
            details.put("storedRecords", repository.count());
        } catch (RuntimeException exception) {
            details.put("reason", "persistence_unavailable");
            details.put("exception", exception.getClass().getSimpleName());
            return Health.outOfService().withDetails(details).build();
        }

        if (!properties.enabled()) {
            return Health.up().withDetails(details).build();
        }

        if (((Number) details.get("divergenceTotal")).doubleValue() > 0.0d
                || ((Number) details.get("errorTotal")).doubleValue() > 0.0d
                || ((Number) details.get("failuresTotal")).doubleValue() > 0.0d) {
            return Health.outOfService().withDetails(details).build();
        }

        return Health.up().withDetails(details).build();
    }

    private double totalRequests(String operation, String resultado) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.local.persistence.requests".equals(meter.getId().getName()))
                .filter(meter -> operation.equals(meter.getId().getTag("operacao")))
                .filter(meter -> resultado.equals(meter.getId().getTag("resultado")))
                .mapToDouble(this::valorContador)
                .sum();
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
