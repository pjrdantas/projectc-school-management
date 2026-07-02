package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveDiagnosticarLeituraLocalDaFatiaDeIdentidadeComFallbackObrigatorio() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("pessoa_identity_local_read_prepared_with_mandatory_fallback");
        assertThat(plan.recommendedNextStep()).isEqualTo("close_phase_64_and_plan_next_people_service_scope");
        assertThat(plan.minimalNextSlice()).isEqualTo("pessoa_identity_read_model");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isTrue();
        assertThat(plan.candidateTables())
                .hasSize(4)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .containsExactly("pessoa", "pessoa_tipo_pessoa");
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("migrationAllowed")
                .containsExactly(true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("backfillAllowed")
                .containsExactly(true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("localReadAllowed")
                .containsExactly(true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", false)
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.requiredHardening()).contains(
                "run-schema-migration-only-with-explicit-opt-in",
                "run-identity-backfill-only-with-explicit-opt-in",
                "keep-read-model-cutover-behind-explicit-flag-and-green-reconciliation",
                "keep-pii-read-model-without-public-exposure",
                "define-reconciliation-by-id-cpf-and-role",
                "keep-monolith-as-authority-for-all-writes");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.migration-enabled",
                "leave-pessoa-identity-read-model-unused-until-backfill-is-green",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy");
    }
}
