package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveMapearProximoDiagnosticoDeEnderecoAposConsultaCadastroFechada() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("address_schema_backfill_diagnostic_closed_no_migration");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_address_schema_migration_opt_in_without_backfill_or_cutover");
        assertThat(plan.minimalNextSlice()).isEqualTo("address_schema_migration_opt_in_no_backfill");
        assertThat(plan.migrationAllowedNow()).isFalse();
        assertThat(plan.backfillAllowedNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateTables())
                .hasSize(7)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .isEmpty();
        assertThat(plan.candidateTables())
                .filteredOn("reason", "consultarCadastro_guarded_read_cutover_closed")
                .extracting("table")
                .containsExactly("aluno", "responsavel", "aluno_responsavel");
        assertThat(plan.candidateTables())
                .filteredOn("reason", "consultarCadastro_guarded_read_cutover_closed")
                .extracting("migrationAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("reason", "consultarCadastro_guarded_read_cutover_closed")
                .extracting("backfillAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("reason", "consultarCadastro_guarded_read_cutover_closed")
                .extracting("localReadAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", false)
                .extracting("table")
                .containsExactly(
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "aluno",
                        "responsavel",
                        "aluno_responsavel",
                        "endereco",
                        "pessoa_endereco");
        assertThat(plan.candidateTables())
                .filteredOn("reason", "not_exposed_by_current_consultarCadastro_contract")
                .extracting("table")
                .isEmpty();
        assertThat(plan.candidateTables())
                .filteredOn("reason", "schema_backfill_diagnostic_closed_migration_not_created")
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.requiredHardening()).contains(
                "keep-consultarCadastro-guarded-local-read-closed",
                "address-schema-columns-defined",
                "reconciliation-key-by-pessoa-endereco-defined",
                "principal-address-rule-defined",
                "address-consumers-mapped-in-monolith-before-local-read-model",
                "internal-address-contract-defined-without-jpa-entities",
                "separate-cep-lookup-from-persisted-address-read-model",
                "viacep-excluded-from-local-read-model-authority",
                "keep-address-orphan-cleanup-on-monolith-until-write-authority-is-defined",
                "keep-pii-read-model-without-public-exposure",
                "keep-monolith-as-authority-for-all-address-writes");
        assertThat(plan.blockedTables()).contains("endereco", "pessoa_endereco");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.migration-enabled",
                "keep-pessoa-identity-local-read-on-monolith-fallback",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green",
                "do-not-create-address-read-model-tables-without-explicit-opt-in");
    }
}
