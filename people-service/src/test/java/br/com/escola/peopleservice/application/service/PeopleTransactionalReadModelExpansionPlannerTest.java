package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveMapearContratoDeConsultaCadastroComCutoverGuarded() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("consultar_cadastro_local_routing_guarded_with_mandatory_fallback");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_consultar_cadastro_guarded_read_cutover_and_monitor_local_read_model");
        assertThat(plan.minimalNextSlice()).isEqualTo("pessoa_student_responsible_local_read_adapter");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.backfillAllowedNow()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isTrue();
        assertThat(plan.candidateTables())
                .hasSize(7)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .containsExactly("aluno", "responsavel", "aluno_responsavel");
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("migrationAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("backfillAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("localReadAllowed")
                .containsExactly(true, true, true);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", false)
                .extracting("table")
                .containsExactly("pessoa", "pessoa_tipo_pessoa", "endereco", "pessoa_endereco");
        assertThat(plan.candidateTables())
                .filteredOn("reason", "not_exposed_by_current_consultarCadastro_contract")
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.requiredHardening()).contains(
                "keep-current-identity-local-read-behind-green-reconciliation",
                "keep-consultarCadastro-on-monolith-when-student-responsible-backfill-is-not-green",
                "do-not-add-address-schema-for-current-consultarCadastro-contract",
                "define-student-responsible-read-model-without-owning-writes",
                "run-student-responsible-schema-migration-only-with-explicit-opt-in",
                "run-student-responsible-backfill-and-reconciliation-only-with-explicit-opt-in",
                "route-consultarCadastro-locally-only-with-mandatory-fallback",
                "keep-pii-read-model-without-public-exposure",
                "define-reconciliation-by-student-responsible-link",
                "keep-monolith-as-authority-for-all-writes");
        assertThat(plan.blockedTables()).contains("endereco", "pessoa_endereco");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.migration-enabled",
                "keep-pessoa-identity-local-read-on-monolith-fallback",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green");
    }
}
