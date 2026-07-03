package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.dto.PeopleLocalReadRoutingDecision;
import br.com.escola.peopleservice.application.dto.PeopleCatalogReadModelSchemaPlan;
import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan;
import br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner;
import br.com.escola.peopleservice.application.service.PeopleLocalPersistenceOperationState;
import br.com.escola.peopleservice.application.service.PeopleLocalReadModelSchemaMigrationState;
import br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard;
import br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("peopleLocalPersistence")
public class PeopleLocalPersistenceHealthIndicator implements HealthIndicator {

    private static final List<String> READ_MODEL_TABLES = List.of(
            "tipo_pessoa",
            "tipo_endereco",
            "pessoa",
            "pessoa_tipo_pessoa",
            "aluno",
            "responsavel",
            "aluno_responsavel");

    private static final List<String> EXCLUDED_AUTHORITATIVE_TABLES = List.of(
            "funcionario",
            "professor",
            "endereco",
            "pessoa_endereco",
            "pessoa_documento");

    private static final List<ReadRouteDescriptor> READ_ROUTES = List.of(
            new ReadRouteDescriptor(
                    "listarTiposPessoa",
                    "GET /internal/v1/pessoas/catalogos/tipos-pessoa",
                    "tipo_pessoa"),
            new ReadRouteDescriptor(
                    "listarTiposEndereco",
                    "GET /internal/v1/pessoas/catalogos/tipos-endereco",
                    "tipo_endereco"),
            new ReadRouteDescriptor(
                    "buscarPorId",
                    "GET /internal/v1/pessoas/{id}",
                    "pessoa"),
            new ReadRouteDescriptor(
                    "consultarCadastro",
                    "GET /internal/v1/pessoas/consulta-cadastral",
                    "aluno,responsavel,aluno_responsavel"));

    private final PeopleLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleLocalReadCutoverGuard readCutoverGuard;
    private final PeopleCatalogReadModelSchemaPlanner catalogSchemaPlanner;
    private final PeopleTransactionalReadModelExpansionPlanner transactionalExpansionPlanner;
    private final PeopleLocalReadModelSchemaMigrationState schemaMigrationState;
    private final PeopleLocalPersistenceOperationState operationState;

    public PeopleLocalPersistenceHealthIndicator(
            PeopleLocalPersistenceProperties properties,
            MeterRegistry meterRegistry,
            PeopleLocalReadCutoverGuard readCutoverGuard,
            PeopleCatalogReadModelSchemaPlanner catalogSchemaPlanner,
            PeopleTransactionalReadModelExpansionPlanner transactionalExpansionPlanner,
            PeopleLocalReadModelSchemaMigrationState schemaMigrationState,
            PeopleLocalPersistenceOperationState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.readCutoverGuard = readCutoverGuard;
        this.catalogSchemaPlanner = catalogSchemaPlanner;
        this.transactionalExpansionPlanner = transactionalExpansionPlanner;
        this.schemaMigrationState = schemaMigrationState;
        this.operationState = operationState;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", properties.enabled());
        details.put("migrationEnabled", properties.migrationEnabled());
        details.put("readModelCutoverEnabled", properties.readModelCutoverEnabled());
        details.put("failOnError", properties.failOnError());
        details.put("backfillEnabled", properties.backfillEnabled());
        details.put("reconciliationEnabled", properties.reconciliationEnabled());
        details.put("backfillBatchSize", properties.backfillBatchSize());
        details.put("readModelFallbackEnabled", properties.readModelFallbackEnabled());
        details.put("mode", "read_only_shadow_foundation");
        details.put("authoritative", false);
        details.put("writeCutoverAllowed", false);
        details.put("readModelTables", READ_MODEL_TABLES);
        details.put("excludedAuthoritativeTables", EXCLUDED_AUTHORITATIVE_TABLES);
        details.put("shadowReadRoutes", diagnosticoRotasLeitura());
        details.put("backfillPlan", diagnosticoBackfill());
        details.put("readRoutingPlan", diagnosticoRoteamentoLeitura());
        details.put("guardedReadCutoverClosure", diagnosticoFechamentoCutoverLeitura());
        details.put("catalogReadModelSchemaPlan", diagnosticoSchemaCatalogo());
        details.put("transactionalReadModelExpansionPlan", diagnosticoExpansaoTransacional());
        details.put("schemaMigration", schemaMigrationState.currentReport());
        details.put("localReadModelBackfill", operationState.currentReport());
        details.put("catalogBackfill", operationState.currentReport());
        details.put("rollbackStrategy", "disable_people.shadow.local-persistence.enabled");
        details.put("backfillRecordsTotal", totalContador("people.shadow.local.persistence.backfill.records"));
        details.put("backfillTablesPlannedTotal",
                totalContador("people.shadow.local.persistence.backfill.tables.planned"));
        details.put("reconciliationDivergencesTotal",
                totalContador("people.shadow.local.persistence.reconciliation.divergences"));
        details.put("reconciliationTablesPlannedTotal",
                totalContador("people.shadow.local.persistence.reconciliation.tables.planned"));
        details.put("failuresTotal", totalContador("people.shadow.local.persistence.failures"));
        details.put("operationCyclesTotal", totalContador("people.shadow.local.persistence.cycles"));
        details.put("readRoutingDecisionsTotal",
                totalContador("people.shadow.local.persistence.read.routing.decisions"));
        details.put("localCatalogReadsTotal",
                totalContador("people.shadow.local.persistence.catalog.reads"));
        details.put("localIdentityReadsTotal",
                totalContador("people.shadow.local.persistence.identity.reads"));
        details.put("localStudentResponsibleReadsTotal",
                totalContador("people.shadow.local.persistence.student.responsible.reads"));
        details.put("schemaMigrationsTotal",
                totalContador("people.shadow.local.persistence.schema.migrations"));

        if (properties.readModelCutoverEnabled()) {
            String reason = primeiraInelegibilidadeRoteamento();
            details.put("reason", reason);
            if (!"read-model-cutover-eligible".equals(reason)) {
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

    private Map<String, Object> diagnosticoFechamentoCutoverLeitura() {
        PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliar("consultarCadastro");
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", "consultarCadastro");
        details.put("shadowRoute", decision.shadowRoute());
        details.put("status", decision.localReadEligible()
                ? "guarded_local_read_enabled"
                : "guarded_local_read_blocked");
        details.put("selectedSource", decision.selectedSource());
        details.put("localCandidateSource", "people_read_model_student_responsible");
        details.put("fallbackSource", "monolith_proxy");
        details.put("fallbackRequired", true);
        details.put("fallbackEnabled", decision.fallbackEnabled());
        details.put("writesEnabled", decision.writesEnabled());
        details.put("reason", decision.reason());
        details.put("greenCriteria", List.of(
                "people.shadow.local-persistence.read-model-cutover-enabled=true",
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "people.shadow.local-persistence.enabled=true",
                "people.shadow.local-persistence.backfill-enabled=true",
                "people.shadow.local-persistence.reconciliation-enabled=true",
                "localReadModelBackfill.status=completed",
                "localReadModelBackfill.divergences=0",
                "people.shadow.local.persistence.reconciliation.divergences=0",
                "people.shadow.local.persistence.failures=0"));
        details.put("metrics", Map.of(
                "routingDecisions", "people.shadow.local.persistence.read.routing.decisions",
                "localReads", "people.shadow.local.persistence.student.responsible.reads",
                "reconciliationDivergences", "people.shadow.local.persistence.reconciliation.divergences",
                "localPersistenceFailures", "people.shadow.local.persistence.failures"));
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                "investigate-people.shadow.local.persistence.student.responsible.reads{result=fallback_error}",
                "rerun-student-responsible-backfill-and-reconciliation-before-reenable",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green"));
        details.put("nextSliceBlocked", "endereco");
        return details;
    }

    private Map<String, Object> diagnosticoSchemaCatalogo() {
        PeopleCatalogReadModelSchemaPlan plan = catalogSchemaPlanner.planejarSchemaCatalogo();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("physicalSchemaRequiredNext", plan.physicalSchemaRequiredNext());
        details.put("localReadAdapterRequiredNext", plan.localReadAdapterRequiredNext());
        details.put("readCutoverAllowed", plan.readCutoverAllowed());
        details.put("writeCutoverAllowed", plan.writeCutoverAllowed());
        details.put("tables", plan.tables());
        details.put("excludedTables", plan.excludedTables());
        details.put("blockers", plan.blockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    private Map<String, Object> diagnosticoExpansaoTransacional() {
        PeopleTransactionalReadModelExpansionPlan plan =
                transactionalExpansionPlanner.planejarProximaFatiaTransacional();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("candidateTables", plan.candidateTables());
        details.put("requiredHardening", plan.requiredHardening());
        details.put("blockedTables", plan.blockedTables());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    private Map<String, Object> diagnosticoRoteamentoLeitura() {
        Map<String, Object> plan = new LinkedHashMap<>();
        readCutoverGuard.avaliarTodas().forEach((operation, decision) -> {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("shadowRoute", decision.shadowRoute());
            detalhe.put("candidateSource", decision.candidateSource());
            detalhe.put("selectedSource", decision.selectedSource());
            detalhe.put("localReadRequested", decision.localReadRequested());
            detalhe.put("localReadEligible", decision.localReadEligible());
            detalhe.put("fallbackEnabled", decision.fallbackEnabled());
            detalhe.put("writesEnabled", decision.writesEnabled());
            detalhe.put("reason", decision.reason());
            plan.put(operation, detalhe);
        });
        return plan;
    }

    private String primeiraInelegibilidadeRoteamento() {
        return readCutoverGuard.avaliarTodas().values().stream()
                .filter(decision -> !decision.localReadEligible())
                .map(PeopleLocalReadRoutingDecision::reason)
                .findFirst()
                .orElse("read-model-cutover-eligible");
    }

    private Map<String, Object> diagnosticoBackfill() {
        Map<String, Object> plano = new LinkedHashMap<>();
        for (String table : READ_MODEL_TABLES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("source", "monolith_proxy");
            detalhe.put("target", "people_read_model_candidate");
            detalhe.put("backfillEnabled", properties.backfillEnabled());
            detalhe.put("reconciliationEnabled", properties.reconciliationEnabled());
            detalhe.put("writesEnabled", false);
            detalhe.put("cutoverEnabled", false);
            detalhe.put("idempotent", true);
            plano.put(table, detalhe);
        }
        return plano;
    }

    private Map<String, Object> diagnosticoRotasLeitura() {
        Map<String, Object> rotas = new LinkedHashMap<>();
        for (ReadRouteDescriptor route : READ_ROUTES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliar(route.operation());
            detalhe.put("shadowRoute", route.shadowRoute());
            detalhe.put("candidateSource", route.candidateSource());
            detalhe.put("currentSource", decision.selectedSource());
            detalhe.put("localReadEnabled", decision.localReadEligible());
            detalhe.put("fallbackRequired", decision.fallbackEnabled());
            detalhe.put("reason", decision.reason());
            rotas.put(route.operation(), detalhe);
        }
        return rotas;
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

    private record ReadRouteDescriptor(String operation, String shadowRoute, String candidateSource) {
    }
}
