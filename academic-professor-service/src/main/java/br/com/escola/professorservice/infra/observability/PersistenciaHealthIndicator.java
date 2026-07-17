package br.com.escola.professorservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.PersistenciaLocalProperties;
import br.com.escola.professorservice.infra.database.entity.AlocacaoSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.TurmaSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.TurmaSyncStateJpaRepository;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("professorShadowPersistence")
public class PersistenciaHealthIndicator implements HealthIndicator {

    private static final List<ReadRouteDescriptor> READ_ROUTES = List.of(
            new ReadRouteDescriptor("listar", "GET /internal/v1/professores", "complete_sync_state_required"),
            new ReadRouteDescriptor("buscarPorId", "GET /internal/v1/professores/{id}", "local_record_presence_required"),
            new ReadRouteDescriptor(
                    "listarAlocacoes",
                    "GET /internal/v1/professores/{id}/turmas-disciplinas",
                    "complete_sync_state_required"),
            new ReadRouteDescriptor(
                    "listarPorTurma",
                    "GET /internal/v1/turmas/{turmaId}/professores",
                    "complete_sync_state_required"));

    private final PersistenciaLocalProperties properties;
    private final CadastroJpaRepository repository;
    private final AlocacaoJpaRepository alocacaoRepository;
    private final CadastroSyncStateJpaRepository syncStateRepository;
    private final AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository;
    private final TurmaSyncStateJpaRepository turmaSyncStateRepository;
    private final MeterRegistry meterRegistry;

    public PersistenciaHealthIndicator(
            PersistenciaLocalProperties properties,
            CadastroJpaRepository repository,
            AlocacaoJpaRepository alocacaoRepository,
            CadastroSyncStateJpaRepository syncStateRepository,
            AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository,
            TurmaSyncStateJpaRepository turmaSyncStateRepository,
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
        details.put("listarCutoverEnabled", properties.listarCutoverEnabled());
        details.put("buscarPorIdCutoverEnabled", properties.buscarPorIdCutoverEnabled());
        details.put("listarAlocacoesCutoverEnabled", properties.listarAlocacoesCutoverEnabled());
        details.put("listarPorTurmaCutoverEnabled", properties.listarPorTurmaCutoverEnabled());
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
            details.put("shadowReadRoutes", diagnosticoRotasLeitura());
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
                CadastroSyncStateJpaEntity::getProfessoresCompletos,
                CadastroSyncStateJpaEntity::getProfessorCount,
                CadastroSyncStateJpaEntity::getSincronizadoEm));
        syncStates.put("alocacoesPorProfessor", resumirSyncState(
                alocacaoSyncStateRepository.findAll(),
                AlocacaoSyncStateJpaEntity::getAlocacoesCompletas,
                AlocacaoSyncStateJpaEntity::getAlocacaoCount,
                AlocacaoSyncStateJpaEntity::getSincronizadoEm));
        syncStates.put("alocacoesPorTurma", resumirSyncState(
                turmaSyncStateRepository.findAll(),
                TurmaSyncStateJpaEntity::getAlocacoesCompletas,
                TurmaSyncStateJpaEntity::getAlocacaoCount,
                TurmaSyncStateJpaEntity::getSincronizadoEm));
        return syncStates;
    }

    private Map<String, Object> diagnosticoRotasLeitura() {
        Map<String, Object> rotas = new LinkedHashMap<>();
        Map<String, Object> syncStates = diagnosticoSyncStates();
        for (ReadRouteDescriptor route : READ_ROUTES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("shadowRoute", route.shadowRoute());
            detalhe.put("readStrategy", estrategiaLeitura(route));
            detalhe.put("localTotal", totalReadRequests(route.operation(), "local"));
            detalhe.put("fallbackTotal", totalReadRequests(route.operation(), "fallback"));
            detalhe.put("disabledTotal", totalReadRequests(route.operation(), "disabled"));
            detalhe.put("fallbackIncompleteSyncStateTotal",
                    totalReadRequests(route.operation(), "fallback", "sync_state_incomplete"));
            detalhe.put("fallbackMissingLocalRecordTotal",
                    totalReadRequests(route.operation(), "fallback", "local_record_missing"));
            detalhe.put("disabledFeatureFlagTotal",
                    totalReadRequests(route.operation(), "disabled", "feature_disabled"));
            detalhe.put("localSyncReadyTotal",
                    totalReadRequests(route.operation(), "local", "sync_state_complete"));
            detalhe.put("localRecordPresentTotal",
                    totalReadRequests(route.operation(), "local", "local_record_present"));

            if ("listar".equals(route.operation())) {
                detalhe.put("syncStateSummary", syncStates.get("professores"));
                detalhe.put("cutoverEnabled", properties.listarCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
                detalhe.put("localCutoverBlockedTotal",
                        totalReadRequests(route.operation(), "local", "cutover_sync_state_incomplete"));
            } else if ("listarAlocacoes".equals(route.operation())) {
                detalhe.put("syncStateSummary", syncStates.get("alocacoesPorProfessor"));
                detalhe.put("cutoverEnabled", properties.listarAlocacoesCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
                detalhe.put("localCutoverBlockedTotal",
                        totalReadRequests(route.operation(), "local", "cutover_sync_state_incomplete"));
            } else if ("listarPorTurma".equals(route.operation())) {
                detalhe.put("syncStateSummary", syncStates.get("alocacoesPorTurma"));
                detalhe.put("cutoverEnabled", properties.listarPorTurmaCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
                detalhe.put("localCutoverBlockedTotal",
                        totalReadRequests(route.operation(), "local", "cutover_sync_state_incomplete"));
            } else if ("buscarPorId".equals(route.operation())) {
                detalhe.put("cutoverEnabled", properties.buscarPorIdCutoverEnabled());
                detalhe.put("rollbackStrategy", "disable_property");
                detalhe.put("localCutoverNotFoundTotal",
                        totalReadRequests(route.operation(), "local", "cutover_local_not_found"));
                detalhe.put("storedProfessorRecords", repository.count());
            }

            rotas.put(route.operation(), detalhe);
        }
        return rotas;
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

    private String estrategiaLeitura(ReadRouteDescriptor route) {
        if ("listar".equals(route.operation()) && properties.listarCutoverEnabled()) {
            return "complete_sync_state_required_no_fallback";
        }
        if ("buscarPorId".equals(route.operation()) && properties.buscarPorIdCutoverEnabled()) {
            return "local_record_presence_required_no_fallback";
        }
        if ("listarAlocacoes".equals(route.operation()) && properties.listarAlocacoesCutoverEnabled()) {
            return "complete_sync_state_required_no_fallback";
        }
        if ("listarPorTurma".equals(route.operation()) && properties.listarPorTurmaCutoverEnabled()) {
            return "complete_sync_state_required_no_fallback";
        }
        return route.readStrategy();
    }

    private double totalRequests(String operation, String resultado) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.local.persistence.requests".equals(meter.getId().getName()))
                .filter(meter -> operation.equals(meter.getId().getTag("operacao")))
                .filter(meter -> resultado.equals(meter.getId().getTag("resultado")))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalReadRequests(String operation, String origem) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.local.read.requests".equals(meter.getId().getName()))
                .filter(meter -> operation.equals(meter.getId().getTag("operacao")))
                .filter(meter -> origem.equals(meter.getId().getTag("origem")))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double totalReadRequests(String operation, String origem, String motivo) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> "professor.shadow.local.read.requests".equals(meter.getId().getName()))
                .filter(meter -> operation.equals(meter.getId().getTag("operacao")))
                .filter(meter -> origem.equals(meter.getId().getTag("origem")))
                .filter(meter -> motivo.equals(meter.getId().getTag("motivo")))
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

    private record ReadRouteDescriptor(String operation, String shadowRoute, String readStrategy) {
    }
}

