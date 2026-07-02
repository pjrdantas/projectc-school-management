package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleCatalogReadModelSchemaPlannerTest {

    @Test
    void deveDiagnosticarCatalogosComBackfillPreparadoSemLiberarCutover() {
        PeopleCatalogReadModelSchemaPlanner planner = new PeopleCatalogReadModelSchemaPlanner();

        var plan = planner.planejarSchemaCatalogo();

        assertThat(plan.status()).isEqualTo("local_catalog_read_adapter_prepared");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_63_and_plan_next_people_service_scope");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.physicalSchemaRequiredNext()).isTrue();
        assertThat(plan.localReadAdapterRequiredNext()).isTrue();
        assertThat(plan.readCutoverAllowed()).isFalse();
        assertThat(plan.writeCutoverAllowed()).isFalse();
        assertThat(plan.tables())
                .hasSize(2)
                .extracting("table")
                .containsExactly("tipo_pessoa", "tipo_endereco");
        assertThat(plan.tables())
                .allSatisfy(table -> {
                    assertThat(table.uniqueKey()).isEqualTo("codigo");
                    assertThat(table.seedSource()).startsWith("monolith.");
                    assertThat(table.includeInFirstMigration()).isTrue();
                });
        assertThat(plan.excludedTables()).contains(
                "pessoa",
                "pessoa_tipo_pessoa",
                "endereco",
                "pessoa_endereco",
                "pessoa_documento");
        assertThat(plan.blockers()).contains(
                "catalog-backfill-and-reconciliation-must-run-green-before-local-read",
                "fallback-to-monolith-remains-mandatory");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "disable-people.shadow.local-persistence.backfill-enabled",
                "disable-people.shadow.local-persistence.reconciliation-enabled");
    }
}
