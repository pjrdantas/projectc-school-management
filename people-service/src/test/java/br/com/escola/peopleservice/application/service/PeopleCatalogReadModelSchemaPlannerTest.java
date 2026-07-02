package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleCatalogReadModelSchemaPlannerTest {

    @Test
    void deveDiagnosticarCatalogosComoPrimeiroSchemaReadOnlySemLiberarMigrationAgora() {
        PeopleCatalogReadModelSchemaPlanner planner = new PeopleCatalogReadModelSchemaPlanner();

        var plan = planner.planejarSchemaCatalogo();

        assertThat(plan.status()).isEqualTo("diagnostic_ready_for_next_migration_preparation");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_opt_in_read_only_schema_for_tipo_pessoa_and_tipo_endereco");
        assertThat(plan.migrationAllowedNow()).isFalse();
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
                "people-service-has-no-datasource-or-flyway-dependency-yet",
                "local-read-adapter-not-implemented",
                "catalog-backfill-and-reconciliation-not-executed");
    }
}
