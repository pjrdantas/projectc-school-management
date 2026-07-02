package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveDiagnosticarProximaFatiaTransacionalSemLiberarSchemaFisicoAinda() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("diagnostic_transactional_slice_not_ready_for_physical_schema");
        assertThat(plan.recommendedNextStep()).isEqualTo("prepare_pessoa_identity_slice_contract_before_migration");
        assertThat(plan.minimalNextSlice()).isEqualTo("pessoa_identity_read_model");
        assertThat(plan.migrationAllowedNow()).isFalse();
        assertThat(plan.backfillAllowedNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateTables())
                .hasSize(4)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .containsExactly("pessoa", "pessoa_tipo_pessoa");
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", false)
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.requiredHardening()).contains(
                "define-pii-field-contract-and-masking-policy",
                "define-reconciliation-by-id-cpf-and-role",
                "keep-monolith-as-authority-for-all-writes");
        assertThat(plan.rollbackSteps()).contains(
                "do-not-create-transactional-migration-in-this-diagnostic-phase",
                "keep-buscarPorId-and-consultarCadastro-on-monolith-proxy");
    }
}
