package br.com.escola.peopleservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalPersistenceHealthIndicatorTest {

    @Test
    void deveReportarFundacaoLocalDesligadaPorPadrao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.shadow.local.persistence.backfill.records")
                .tag("tabela", "pessoa")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(3.0d);
        Counter.builder("people.shadow.local.persistence.reconciliation.divergences")
                .tag("tabela", "pessoa")
                .tag("tipo", "cpf")
                .register(meterRegistry)
                .increment();

        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                        meterRegistry),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", false)
                .containsEntry("migrationEnabled", false)
                .containsEntry("readModelCutoverEnabled", false)
                .containsEntry("backfillEnabled", false)
                .containsEntry("reconciliationEnabled", false)
                .containsEntry("backfillBatchSize", 500)
                .containsEntry("readModelFallbackEnabled", true)
                .containsEntry("authoritative", false)
                .containsEntry("writeCutoverAllowed", false)
                .containsEntry("mode", "read_only_shadow_foundation")
                .containsEntry("rollbackStrategy", "disable_people.shadow.local-persistence.enabled")
                .containsEntry("backfillRecordsTotal", 3.0d)
                .containsEntry("backfillTablesPlannedTotal", 0.0d)
                .containsEntry("reconciliationDivergencesTotal", 1.0d)
                .containsEntry("reconciliationTablesPlannedTotal", 0.0d)
                .containsEntry("operationCyclesTotal", 0.0d)
                .containsEntry("readRoutingDecisionsTotal", 0.0d)
                .containsEntry("failuresTotal", 0.0d);

        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        assertThat(buscarPorId)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/{id}")
                .containsEntry("candidateSource", "pessoa")
                .containsEntry("currentSource", "monolith_proxy")
                .containsEntry("localReadEnabled", false)
                .containsEntry("fallbackRequired", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> readRoutingPlan = (Map<String, Object>) health.getDetails().get("readRoutingPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorIdRouting = (Map<String, Object>) readRoutingPlan.get("buscarPorId");
        assertThat(buscarPorIdRouting)
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadRequested", false)
                .containsEntry("localReadEligible", false)
                .containsEntry("fallbackEnabled", true)
                .containsEntry("writesEnabled", false)
                .containsEntry("reason", "read-model-cutover-disabled");

        @SuppressWarnings("unchecked")
        java.util.List<String> readModelTables = (java.util.List<String>) health.getDetails().get("readModelTables");
        assertThat(readModelTables)
                .containsExactly(
                        "tipo_pessoa",
                        "tipo_endereco",
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "endereco",
                        "pessoa_endereco");

        @SuppressWarnings("unchecked")
        Map<String, Object> backfillPlan = (Map<String, Object>) health.getDetails().get("backfillPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> pessoaPlan = (Map<String, Object>) backfillPlan.get("pessoa");
        assertThat(pessoaPlan)
                .containsEntry("source", "monolith_proxy")
                .containsEntry("target", "people_read_model_candidate")
                .containsEntry("backfillEnabled", false)
                .containsEntry("reconciliationEnabled", false)
                .containsEntry("writesEnabled", false)
                .containsEntry("cutoverEnabled", false)
                .containsEntry("idempotent", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> catalogSchemaPlan =
                (Map<String, Object>) health.getDetails().get("catalogReadModelSchemaPlan");
        assertThat(catalogSchemaPlan)
                .containsEntry("status", "diagnostic_ready_for_next_migration_preparation")
                .containsEntry("recommendedNextStep",
                        "prepare_opt_in_read_only_schema_for_tipo_pessoa_and_tipo_endereco")
                .containsEntry("migrationAllowedNow", false)
                .containsEntry("physicalSchemaRequiredNext", true)
                .containsEntry("localReadAdapterRequiredNext", true)
                .containsEntry("readCutoverAllowed", false)
                .containsEntry("writeCutoverAllowed", false);
    }

    @Test
    void deveReportarOutOfServiceQuandoPersistenciaLocalForLigadaAntesDoSchemaEBackfill() {
        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(true, false, false, false, false, false, 500, true),
                new SimpleMeterRegistry(),
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(true, false, false, false, false, false, 500, true),
                        new SimpleMeterRegistry()),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("enabled", true)
                .containsEntry("reason", "local-persistence-foundation-only")
                .containsEntry("writeCutoverAllowed", false);
    }

    @Test
    void deveReportarOutOfServiceQuandoCutoverLocalForLigadoAntesDaImplementacao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(true, true, true, true, true, true, 100, true),
                meterRegistry,
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(true, true, true, true, true, true, 100, true),
                        meterRegistry),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("readModelCutoverEnabled", true)
                .containsEntry("reason", "local-read-adapter-not-configured")
                .containsEntry("authoritative", false);
    }
}
