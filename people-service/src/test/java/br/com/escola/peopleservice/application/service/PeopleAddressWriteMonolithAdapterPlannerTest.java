package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleAddressWriteMonolithAdapterPlannerTest {

    @Test
    void deveBloquearAdapterEnquantoMonolitoNaoTemContratoHttpInternoDeEscrita() {
        PeopleAddressWriteMonolithAdapterPlanner planner = new PeopleAddressWriteMonolithAdapterPlanner();

        var plan = planner.planejarAdapterEscritaMonolito();

        assertThat(plan.phase()).isEqualTo("Fase 72");
        assertThat(plan.slice()).isEqualTo("address_write_monolith_adapter_diagnostic");
        assertThat(plan.status()).isEqualTo("monolith_http_write_contract_missing_adapter_blocked");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("define_monolith_internal_address_write_http_contract_before_adapter");
        assertThat(plan.minimalNextSlice()).isEqualTo("monolith_internal_address_write_contract_no_people_adapter");
        assertThat(plan.monolithHttpWriteContractAvailable()).isFalse();
        assertThat(plan.adapterImplementationAllowedNow()).isFalse();
        assertThat(plan.writeCutoverAllowedNow()).isFalse();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.candidateOperations())
                .hasSize(2)
                .allSatisfy(operation -> assertThat(operation.adapterAllowedNow()).isFalse());
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
                "implement-people-service-monolith-write-client",
                "local-address-write-persistence",
                "move-create-person-with-address-flow");
    }
}
