package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleAddressWriteMonolithAdapterPlannerTest {

    @Test
    void deveLiberarDiagnosticoDoAdapterAposContratoHttpInternoDoMonolito() {
        PeopleAddressWriteMonolithAdapterPlanner planner = new PeopleAddressWriteMonolithAdapterPlanner();

        var plan = planner.planejarAdapterEscritaMonolito();

        assertThat(plan.phase()).isEqualTo("Fase 72");
        assertThat(plan.slice()).isEqualTo("address_write_monolith_adapter_diagnostic");
        assertThat(plan.status()).isEqualTo("monolith_http_write_contract_defined_adapter_not_connected");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("evaluate_people_service_monolith_write_adapter_behind_guard");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_service_monolith_write_adapter_guarded_no_local_persistence");
        assertThat(plan.monolithHttpWriteContractAvailable()).isTrue();
        assertThat(plan.adapterImplementationAllowedNow()).isTrue();
        assertThat(plan.writeCutoverAllowedNow()).isFalse();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.candidateOperations())
                .hasSize(2)
                .allSatisfy(operation -> assertThat(operation.adapterAllowedNow()).isTrue());
        assertThat(plan.candidateOperations())
                .extracting("operation")
                .containsExactly(
                        "create-or-update-principal-address",
                        "cleanup-person-address-links-and-orphans");
        assertThat(plan.requiredMonolithContracts()).contains(
                "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                "DELETE /internal/pessoas/{pessoaId}/enderecos",
                "Idempotency-Key header required");
        assertThat(plan.consistencyBlockers()).contains(
                "address-write-is-still-coupled-to-person-update-transaction",
                "people-service-local-read-model-is-not-write-authority");
        assertThat(plan.explicitlyOutOfScope()).contains(
                "activate-address-write-cutover",
                "local-address-write-persistence",
                "move-create-person-with-address-flow");
    }
}
