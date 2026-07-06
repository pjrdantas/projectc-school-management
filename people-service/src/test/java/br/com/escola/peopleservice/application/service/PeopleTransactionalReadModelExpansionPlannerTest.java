package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleTransactionalReadModelExpansionPlannerTest {

    @Test
    void deveMapearProximoDiagnosticoDeEnderecoAposConsultaCadastroFechada() {
        PeopleTransactionalReadModelExpansionPlanner planner = new PeopleTransactionalReadModelExpansionPlanner();

        var plan = planner.planejarProximaFatiaTransacional();

        assertThat(plan.status()).isEqualTo("address_local_read_routing_operation_defined_no_connection");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("evaluate_address_adapter_connection_behind_guard_no_route_change");
        assertThat(plan.minimalNextSlice()).isEqualTo("address_adapter_connection_guarded_diagnostic_no_external_route");
        assertThat(plan.migrationAllowedNow()).isTrue();
        assertThat(plan.backfillAllowedNow()).isTrue();
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
                .filteredOn("reason", "address_local_read_routing_operation_defined_no_connection")
                .extracting("table")
                .containsExactly("endereco", "pessoa_endereco");
        assertThat(plan.candidateTables())
                .filteredOn("reason", "address_local_read_routing_operation_defined_no_connection")
                .extracting("supportedOperations")
                .containsExactly(
                        java.util.List.of("addressLocalRead"),
                        java.util.List.of("addressLocalRead"));
        assertThat(plan.candidateTables())
                .filteredOn("reason", "address_local_read_routing_operation_defined_no_connection")
                .extracting("migrationAllowed")
                .containsExactly(true, true);
        assertThat(plan.candidateTables())
                .filteredOn("reason", "address_local_read_routing_operation_defined_no_connection")
                .extracting("backfillAllowed")
                .containsExactly(true, true);
        assertThat(plan.requiredHardening()).contains(
                "keep-consultarCadastro-guarded-local-read-closed",
                "phase-68-formally-closed",
                "address-schema-migration-opt-in-prepared",
                "address-backfill-reconciliation-opt-in-prepared",
                "address-local-read-contract-diagnostic-started",
                "address-local-read-port-and-dto-prepared",
                "address-local-read-jdbc-adapter-prepared",
                "phase-69-formally-closed",
                "address-read-cutover-eligibility-diagnostic-started",
                "address-local-read-routing-operation-defined",
                "address-local-read-routing-metrics-defined",
                "address-read-cutover-must-not-connect-query-service-yet",
                "address-local-read-payload-must-be-internal-only",
                "address-local-read-guard-must-stay-independent-from-consultarCadastro",
                "address-schema-columns-defined",
                "reconciliation-key-by-pessoa-endereco-defined",
                "principal-address-rule-defined",
                "multiple-principal-addresses-block-green-reconciliation",
                "address-consumers-mapped-in-monolith-before-local-read-model",
                "internal-address-contract-defined-without-jpa-entities",
                "separate-cep-lookup-from-persisted-address-read-model",
                "viacep-excluded-from-local-read-model-authority",
                "keep-address-orphan-cleanup-on-monolith-until-write-authority-is-defined",
                "keep-pii-read-model-without-public-exposure",
                "keep-monolith-as-authority-for-all-address-writes");
        assertThat(plan.blockedTables()).contains("endereco", "pessoa_endereco");
        assertThat(plan.rollbackSteps()).contains(
                "do-not-add-address-route-or-bff-cutover-in-this-phase",
                "do-not-connect-address-adapter-to-query-service-in-this-phase",
                "disable-people.shadow.local-persistence.migration-enabled",
                "keep-pessoa-identity-local-read-on-monolith-fallback",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green",
                "disable-people.shadow.local-persistence.backfill-enabled",
                "disable-people.shadow.local-persistence.reconciliation-enabled");
    }
}
