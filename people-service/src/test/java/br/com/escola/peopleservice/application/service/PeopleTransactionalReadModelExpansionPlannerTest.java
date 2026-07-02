package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveDiagnosticarProximaFatiaDeEnderecoSemCutover() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("pessoa_address_diagnostic_prepared_without_cutover");
        assertThat(plan.recommendedNextStep()).isEqualTo("prepare_address_schema_opt_in_for_consultar_cadastro");
        assertThat(plan.minimalNextSlice()).isEqualTo("pessoa_address_read_model");
        assertThat(plan.migrationAllowedNow()).isFalse();
        assertThat(plan.backfillAllowedNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateTables())
                .hasSize(4)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("migrationAllowed")
                .containsExactly(false, false);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("backfillAllowed")
                .containsExactly(false, false);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("localReadAllowed")
                .containsExactly(false, false);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", false)
                .extracting("table")
                .containsExactly("pessoa", "pessoa_tipo_pessoa");
        assertThat(plan.requiredHardening()).contains(
                "keep-current-identity-local-read-behind-green-reconciliation",
                "map-consultarCadastro-response-before-schema",
                "define-address-read-model-without-owning-address-writes",
                "keep-address-schema-migration-for-next-subphase",
                "keep-consultarCadastro-on-monolith-until-address-backfill-is-green",
                "keep-pii-read-model-without-public-exposure",
                "define-reconciliation-by-address-id-and-person-address-link",
                "keep-monolith-as-authority-for-all-writes");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.migration-enabled",
                "keep-pessoa-identity-local-read-on-monolith-fallback",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy");
    }
}
