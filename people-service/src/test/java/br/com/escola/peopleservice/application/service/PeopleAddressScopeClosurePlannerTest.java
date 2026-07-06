package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleAddressScopeClosurePlannerTest {

    @Test
    void devePlanejarFechamentoDoEscopoPessoaEnderecoComProximaFamiliaPreferida() {
        PeopleAddressScopeClosurePlanner planner = new PeopleAddressScopeClosurePlanner();

        var plan = planner.planejarFechamentoEscopoPessoaEndereco();

        assertThat(plan.phase()).isEqualTo("Fase 73");
        assertThat(plan.slice()).isEqualTo("people_address_scope_closure_review");
        assertThat(plan.status()).isEqualTo("people_address_scope_review_closed_ready_for_next_family_diagnostic");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("start_people_document_scope_diagnostic_without_reopening_address_cutover");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_contract_diagnostic");
        assertThat(plan.readScopeClosed()).isTrue();
        assertThat(plan.writeScopePreparedWithoutCutover()).isTrue();
        assertThat(plan.activationRequiredNow()).isFalse();
        assertThat(plan.safeToStartNextFamilyDiagnostic()).isTrue();
        assertThat(plan.closedCapabilities()).contains(
                "consultarCadastro local read cutover closed with mandatory fallback",
                "MonolithPessoaAddressWriteClient implemented behind disabled-by-default guard");
        assertThat(plan.remainingActivationBlockers()).contains(
                "create-person-with-address remains inside monolith person transaction",
                "address write adapter guard must remain disabled until a future explicit activation phase");
        assertThat(plan.nextFamilyCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.family()).isEqualTo("pessoa_documento");
            assertThat(candidate.status()).isEqualTo("next_minimal_candidate");
            assertThat(candidate.allowedNow()).isTrue();
        });
    }
}
