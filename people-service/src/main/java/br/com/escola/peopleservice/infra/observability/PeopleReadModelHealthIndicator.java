package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.PeopleReadSourceDecision;
import br.com.escola.peopleservice.application.service.PeopleReadModelSyncState;
import br.com.escola.peopleservice.application.service.PeopleReadModelMigrationState;
import br.com.escola.peopleservice.application.service.PeopleReadSourcePolicy;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("peopleReadModel")
public class PeopleReadModelHealthIndicator implements HealthIndicator {

    private static final List<String> READ_MODEL_TABLES = List.of(
            "tipo_pessoa",
            "tipo_endereco",
            "status_aluno",
            "parentesco",
            "pessoa",
            "pessoa_tipo_pessoa",
            "aluno",
            "responsavel",
            "aluno_responsavel",
            "endereco",
            "pessoa_endereco",
            "people_documento_read_model",
            "people_funcionario_read_model",
            "people_professor_read_model");

    private static final List<String> EXCLUDED_WRITE_AUTHORITIES = List.of(
            "funcionario",
            "professor",
            "pessoa_documento");

    private final PeopleReadModelProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleReadSourcePolicy readRoutingPolicy;
    private final PeopleReadModelMigrationState schemaMigrationState;
    private final PeopleReadModelSyncState operationState;

    public PeopleReadModelHealthIndicator(
            PeopleReadModelProperties properties,
            MeterRegistry meterRegistry,
            PeopleReadSourcePolicy readRoutingPolicy,
            PeopleReadModelMigrationState schemaMigrationState,
            PeopleReadModelSyncState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.readRoutingPolicy = readRoutingPolicy;
        this.schemaMigrationState = schemaMigrationState;
        this.operationState = operationState;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", properties.enabled());
        details.put("migrationEnabled", properties.migrationEnabled());
        details.put("localReadRoutingEnabled", properties.localReadRoutingEnabled());
        details.put("failOnError", properties.failOnError());
        details.put("backfillEnabled", properties.backfillEnabled());
        details.put("reconciliationEnabled", properties.reconciliationEnabled());
        details.put("backfillBatchSize", properties.backfillBatchSize());
        details.put("fallbackEnabled", properties.fallbackEnabled());
        details.put("operationalMode", properties.enabled() ? "local_read_model" : "monolith_only");
        details.put("authoritativeWriteStorage", false);
        details.put("readModelTables", READ_MODEL_TABLES);
        details.put("excludedWriteAuthorities", EXCLUDED_WRITE_AUTHORITIES);
        details.put("coreReadRoutes", mapDecisions(readRoutingPolicy.avaliarTodas()));
        details.put("internalReadRoutes", internalReadRoutes());
        details.put("schemaMigration", schemaMigrationState.currentReport());
        details.put("synchronizationReport", operationState.currentReport());
        details.put("rollbackStrategy", "disable_people.read-model.enabled");
        details.put("backfillRecordsTotal", totalContador("people.readmodel.sync.records"));
        details.put("backfillTablesPlannedTotal",
                totalContador("people.readmodel.sync.tables.planned"));
        details.put("reconciliationDivergencesTotal",
                totalContador("people.readmodel.sync.divergences"));
        details.put("reconciliationTablesPlannedTotal",
                totalContador("people.readmodel.sync.reconciliation.tables"));
        details.put("failuresTotal", totalContador("people.readmodel.sync.failures"));
        details.put("operationCyclesTotal", totalContador("people.readmodel.sync.cycles"));
        details.put("readRoutingDecisionsTotal",
                totalContador("people.read.routing.decisions"));
        details.put("localCatalogReadsTotal",
                totalContador("people.catalog.reads"));
        details.put("localIdentityReadsTotal",
                totalContador("people.identity.reads"));
        details.put("localStudentResponsibleReadsTotal",
                totalContador("people.studentresponsible.reads"));
        details.put("addressReadRoutingDecisionsTotal",
                totalContador("people.address.read.routing.decisions"));
        details.put("localAddressReadsTotal",
                totalContador("people.address.reads"));
        details.put("documentMetadataReadRoutingDecisionsTotal",
                totalContador("people.document.read.routing.decisions"));
        details.put("localDocumentMetadataReadsTotal",
                totalContador("people.document.reads"));
        details.put("responsibleReadRoutingDecisionsTotal",
                totalContador("people.responsible.read.routing.decisions"));
        details.put("localResponsibleLookupsTotal",
                totalContador("people.responsible.lookup"));
        details.put("funcionarioReadRoutingDecisionsTotal",
                totalContador("people.funcionario.read.routing.decisions"));
        details.put("localFuncionarioResumoReadsTotal",
                totalContador("people.funcionario.reads"));
        details.put("professorReadRoutingDecisionsTotal",
                totalContador("people.professor.read.routing.decisions"));
        details.put("localProfessorResumoReadsTotal",
                totalContador("people.professor.reads"));
        details.put("addressWriteCommandsTotal",
                totalContador("people.address.write.commands"));
        details.put("monolithAddressWriteRequestsTotal",
                totalContador("people.monolith.address.write.requests"));
        details.put("monolithAddressWriteFailuresTotal",
                totalContador("people.monolith.address.write.failures"));
        details.put("schemaMigrationsTotal",
                totalContador("people.readmodel.migrations"));

        if (properties.localReadRoutingEnabled()) {
            String reason = primeiraRestricaoDeRoteamento();
            details.put("reason", reason);
            if (!"local-read-routing-eligible".equals(reason)) {
                return Health.outOfService().withDetails(details).build();
            }
            return Health.up().withDetails(details).build();
        }

        if (properties.enabled()) {
            details.put("reason", "local-persistence-foundation-only");
            return Health.outOfService().withDetails(details).build();
        }

        return Health.up().withDetails(details).build();
    }

    private Map<String, Object> mapDecisions(Map<String, PeopleReadSourceDecision> decisions) {
        Map<String, Object> details = new LinkedHashMap<>();
        decisions.forEach((operation, decision) -> details.put(operation, mapDecision(decision)));
        return details;
    }

    private Map<String, Object> internalReadRoutes() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("address", mapDecision(readRoutingPolicy.avaliarLeituraEndereco()));
        details.put("documentMetadata", mapDecision(readRoutingPolicy.avaliarLeituraDocumentoMetadata()));
        details.put("responsibleLink", mapDecision(readRoutingPolicy.avaliarLeituraResponsavelVinculo()));
        details.put("funcionarioSummary", mapDecision(readRoutingPolicy.avaliarLeituraFuncionarioResumo()));
        details.put("professorSummary", mapDecision(readRoutingPolicy.avaliarLeituraProfessorResumo()));
        return details;
    }

    private Map<String, Object> mapDecision(PeopleReadSourceDecision decision) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("route", decision.route());
        details.put("candidateSource", decision.candidateSource());
        details.put("selectedSource", decision.selectedSource());
        details.put("localReadRequested", decision.localReadRequested());
        details.put("localReadEligible", decision.localReadEligible());
        details.put("fallbackEnabled", decision.fallbackEnabled());
        details.put("writesEnabled", decision.writesEnabled());
        details.put("reason", decision.reason());
        return details;
    }

    private String primeiraRestricaoDeRoteamento() {
        return readRoutingPolicy.avaliarTodas().values().stream()
                .map(PeopleReadSourceDecision::reason)
                .filter(reason -> !reason.endsWith("-eligible"))
                .findFirst()
                .orElse("local-read-routing-eligible");
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

