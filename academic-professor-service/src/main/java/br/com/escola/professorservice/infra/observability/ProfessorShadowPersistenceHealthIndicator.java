package br.com.escola.professorservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowPersistence")
public class ProfessorShadowPersistenceHealthIndicator implements HealthIndicator {

    private final ProfessorShadowLocalPersistenceProperties properties;
    private final ProfessorShadowJpaRepository repository;
    private final ProfessorAlocacaoShadowJpaRepository alocacaoRepository;
    private final MeterRegistry meterRegistry;

    public ProfessorShadowPersistenceHealthIndicator(
            ProfessorShadowLocalPersistenceProperties properties,
            ProfessorShadowJpaRepository repository,
            ProfessorAlocacaoShadowJpaRepository alocacaoRepository,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", properties.enabled());
        details.put("failOnError", properties.failOnError());
        details.put("requestsTotal", totalContador("professor.shadow.local.persistence.requests"));
        details.put("createSuccessTotal", totalRequests("criar", "success"));
        details.put("allocateSuccessTotal", totalRequests("vincularTurmaDisciplina", "success"));
        details.put("successTotal", ((Number) details.get("createSuccessTotal")).doubleValue()
                + ((Number) details.get("allocateSuccessTotal")).doubleValue());
        details.put("createSkippedDisabledTotal", totalRequests("criar", "skipped_disabled"));
        details.put("allocateSkippedDisabledTotal", totalRequests("vincularTurmaDisciplina", "skipped_disabled"));
        details.put("skippedDisabledTotal", ((Number) details.get("createSkippedDisabledTotal")).doubleValue()
                + ((Number) details.get("allocateSkippedDisabledTotal")).doubleValue());
        details.put("createDivergenceTotal", totalRequests("criar", "divergence"));
        details.put("allocateDivergenceTotal", totalRequests("vincularTurmaDisciplina", "divergence"));
        details.put("divergenceTotal", ((Number) details.get("createDivergenceTotal")).doubleValue()
                + ((Number) details.get("allocateDivergenceTotal")).doubleValue());
        details.put("createErrorTotal", totalRequests("criar", "error"));
        details.put("allocateErrorTotal", totalRequests("vincularTurmaDisciplina", "error"));
        details.put("errorTotal", ((Number) details.get("createErrorTotal")).doubleValue()
                + ((Number) details.get("allocateErrorTotal")).doubleValue());
        details.put("failuresTotal", totalContador("professor.shadow.local.persistence.failures"));

        try {
            details.put("storedProfessorRecords", repository.count());
            details.put("storedAllocationRecords", alocacaoRepository.count());
            details.put("storedRecords", ((Number) details.get("storedProfessorRecords")).longValue()
                    + ((Number) details.get("storedAllocationRecords")).longValue());
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
