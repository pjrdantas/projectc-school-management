package br.com.escola.professorservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorTurmaShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorTurmaShadowSyncStateJpaRepository;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowPersistence")
public class ProfessorShadowPersistenceHealthIndicator implements HealthIndicator {

    private final ProfessorShadowLocalPersistenceProperties properties;
    private final ProfessorShadowJpaRepository repository;
    private final ProfessorAlocacaoShadowJpaRepository alocacaoRepository;
    private final ProfessorShadowSyncStateJpaRepository syncStateRepository;
    private final ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository;
    private final ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository;
    private final MeterRegistry meterRegistry;

    public ProfessorShadowPersistenceHealthIndicator(
            ProfessorShadowLocalPersistenceProperties properties,
            ProfessorShadowJpaRepository repository,
            ProfessorAlocacaoShadowJpaRepository alocacaoRepository,
            ProfessorShadowSyncStateJpaRepository syncStateRepository,
            ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository,
            ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.syncStateRepository = syncStateRepository;
        this.alocacaoSyncStateRepository = alocacaoSyncStateRepository;
        this.turmaSyncStateRepository = turmaSyncStateRepository;
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
            details.put("shadowSyncStates", diagnosticoSyncStates());
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

    private Map<String, Object> diagnosticoSyncStates() {
        Map<String, Object> syncStates = new LinkedHashMap<>();
        syncStates.put("professores", resumirSyncState(
                syncStateRepository.findAll(),
                ProfessorShadowSyncStateJpaEntity::getProfessoresCompletos,
                ProfessorShadowSyncStateJpaEntity::getProfessorCount,
                ProfessorShadowSyncStateJpaEntity::getSincronizadoEm));
        syncStates.put("alocacoesPorProfessor", resumirSyncState(
                alocacaoSyncStateRepository.findAll(),
                ProfessorAlocacaoShadowSyncStateJpaEntity::getAlocacoesCompletas,
                ProfessorAlocacaoShadowSyncStateJpaEntity::getAlocacaoCount,
                ProfessorAlocacaoShadowSyncStateJpaEntity::getSincronizadoEm));
        syncStates.put("alocacoesPorTurma", resumirSyncState(
                turmaSyncStateRepository.findAll(),
                ProfessorTurmaShadowSyncStateJpaEntity::getAlocacoesCompletas,
                ProfessorTurmaShadowSyncStateJpaEntity::getAlocacaoCount,
                ProfessorTurmaShadowSyncStateJpaEntity::getSincronizadoEm));
        return syncStates;
    }

    private <T> Map<String, Object> resumirSyncState(
            java.util.List<T> syncStates,
            Function<T, Boolean> completoExtractor,
            Function<T, Long> countExtractor,
            Function<T, java.time.LocalDateTime> sincronizadoEmExtractor) {
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("trackedTotal", syncStates.size());
        resumo.put("completeTotal", syncStates.stream()
                .filter(state -> Boolean.TRUE.equals(completoExtractor.apply(state)))
                .count());
        resumo.put("incompleteTotal", syncStates.stream()
                .filter(state -> !Boolean.TRUE.equals(completoExtractor.apply(state)))
                .count());
        resumo.put("trackedRecordsTotal", syncStates.stream()
                .map(countExtractor)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum());
        resumo.put("lastSynchronizedAt", syncStates.stream()
                .map(sincronizadoEmExtractor)
                .filter(java.util.Objects::nonNull)
                .max(java.time.LocalDateTime::compareTo)
                .map(java.time.LocalDateTime::toString)
                .orElse(null));
        return resumo;
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
