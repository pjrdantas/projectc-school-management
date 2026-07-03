package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveMapearContratoDeConsultaCadastroSemCutover() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("consultar_cadastro_contract_mapped_without_cutover");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_student_responsible_schema_opt_in_for_consultar_cadastro");
        assertThat(plan.minimalNextSlice()).isEqualTo("pessoa_student_responsible_read_model");
        assertThat(plan.migrationAllowedNow()).isFalse();
        assertThat(plan.backfillAllowedNow()).isFalse();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.candidateTables())
                .hasSize(7)
                .filteredOn("includeInNextSlice", true)
                .extracting("table")
                .containsExactly("aluno", "responsavel", "aluno_responsavel");
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("migrationAllowed")
                .containsExactly(false, false, false);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("backfillAllowed")
                .containsExactly(false, false, false);
        assertThat(plan.candidateTables())
                .filteredOn("includeInNextSlice", true)
                .extracting("localReadAllowed")
                .containsExactly(false, false, false);
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
                "keep-consultarCadastro-on-monolith-until-student-responsible-backfill-is-green",
                "do-not-add-address-schema-for-current-consultarCadastro-contract",
                "define-student-responsible-read-model-without-owning-writes",
                "keep-student-responsible-schema-migration-for-next-subphase",
                "keep-pii-read-model-without-public-exposure",
                "define-reconciliation-by-student-responsible-link",
                "keep-monolith-as-authority-for-all-writes");
        assertThat(plan.blockedTables()).contains("endereco", "pessoa_endereco");
        assertThat(plan.rollbackSteps()).contains(
                "disable-people.shadow.local-persistence.migration-enabled",
                "keep-pessoa-identity-local-read-on-monolith-fallback",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy");
    }
}
