package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleAddressWriteMonolithAdapterPlannerTest {

    @Test
    void deveReportarAdapterPreparadoComGuardDesligadoSemCutover() {
        PeopleAddressWriteMonolithAdapterPlanner planner = new PeopleAddressWriteMonolithAdapterPlanner();

        var plan = planner.planejarAdapterEscritaMonolito();

        assertThat(plan.phase()).isEqualTo("Fase 72");
        assertThat(plan.slice()).isEqualTo("address_write_monolith_adapter_diagnostic");
        assertThat(plan.status()).isEqualTo("monolith_write_adapter_prepared_guard_disabled_no_cutover");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_72_and_plan_next_people_backend_scope");
        assertThat(plan.minimalNextSlice()).isEqualTo("phase_72_closure_no_write_cutover");
        assertThat(plan.monolithHttpWriteContractAvailable()).isTrue();
        assertThat(plan.adapterImplementationAllowedNow()).isTrue();
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
        assertThat(plan.guardPreconditions()).contains(
                "MonolithPessoaAddressWriteClient implemented with HTTP PUT and DELETE contracts",
                "people.shadow.monolith.address-write-adapter-enabled defaults to false");
        assertThat(plan.consistencyBlockers()).contains(
                "address-write-is-still-coupled-to-person-update-transaction",
                "people-service-local-read-model-is-not-write-authority");
        assertThat(plan.explicitlyOutOfScope()).contains(
                "activate-address-write-cutover",
                "local-address-write-persistence",
                "move-create-person-with-address-flow");
    }
}
