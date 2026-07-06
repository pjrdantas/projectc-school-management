package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleAddressWriteAuthorityPlannerTest {

    @Test
    void deveMapearDiagnosticoDeAutoridadeDeEscritaDeEnderecoSemCutover() {
        PeopleAddressWriteAuthorityPlanner planner = new PeopleAddressWriteAuthorityPlanner();

        var plan = planner.planejarAutoridadeEscritaEndereco();

        assertThat(plan.phase()).isEqualTo("Fase 71");
        assertThat(plan.slice()).isEqualTo("endereco_write_authority");
        assertThat(plan.status()).isEqualTo("diagnostic_started_no_write_cutover");
        assertThat(plan.recommendedNextStep()).isEqualTo("define_address_write_command_contract_no_external_route");
        assertThat(plan.minimalNextSlice()).isEqualTo("address_write_command_contract_diagnostic_no_persistence_change");
        assertThat(plan.writeCutoverAllowedNow()).isFalse();
        assertThat(plan.localReadPrerequisiteClosed()).isTrue();
        assertThat(plan.candidateOperations())
                .hasSize(4)
                .allSatisfy(operation -> assertThat(operation.allowedNow()).isFalse());
        assertThat(plan.candidateOperations())
                .extracting("operation")
                .containsExactly(
                        "create-person-with-principal-address",
                        "update-person-principal-address",
                        "remove-person-address-links-and-orphans",
                        "cep-lookup-for-address-input");
        assertThat(plan.monolithWriteAuthorities()).contains(
                "PessoaFoundationService.criarPessoaComTipoEEndereco",
                "PessoaFoundationService.atualizarPessoaEEndereco",
                "PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                "ViaCepService");
        assertThat(plan.requiredContracts()).contains(
                "PeopleAddressWritePort command payload without JPA entities",
                "orphan cleanup contract with shared-address safeguard",
                "monolith fallback contract before any write routing");
        assertThat(plan.consistencyBlockers()).contains(
                "address-write-is-coupled-to-person-create-update-transaction",
                "local-read-model-is-not-write-authority");
        assertThat(plan.explicitlyOutOfScope()).contains(
                "create-address-write-rest-route",
                "bff-route-change",
                "frontend-change",
                "write-to-local-address-tables");
    }
}
