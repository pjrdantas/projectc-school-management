package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleContactScopeClosurePlannerTest {

    @Test
    void devePlanejarFechamentoDoEscopoContato() {
        PeopleContactScopeClosurePlanner planner = new PeopleContactScopeClosurePlanner();

        var plan = planner.planejarFechamentoEscopoContato();

        assertThat(plan.phase()).isEqualTo("Fase 94");
        assertThat(plan.slice()).isEqualTo("people_contact_scope_closure_review");
        assertThat(plan.status()).isEqualTo("people_contact_scope_review_closed_ready_for_next_family_diagnostic");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("start_next_backend_family_without_reopening_people_contact");
        assertThat(plan.minimalNextSlice()).isEqualTo("next_backend_family_diagnostic");
        assertThat(plan.readScopeClosed()).isTrue();
        assertThat(plan.writeScopePreparedWithoutCutover()).isFalse();
        assertThat(plan.activationRequiredNow()).isFalse();
        assertThat(plan.safeToStartNextFamilyDiagnostic()).isTrue();
        assertThat(plan.closedCapabilities()).contains(
                "local read service and JDBC adapter prepared over pessoa read model",
                "contact internal usage diagnostic completed without forcing an artificial consumer");
        assertThat(plan.remainingActivationBlockers()).contains(
                "no justified internal consumer exists in people-service for contact at this stage");
        assertThat(plan.nextFamilyCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.family()).isEqualTo("next_backend_family");
            assertThat(candidate.status()).isEqualTo("allowed_now");
            assertThat(candidate.allowedNow()).isTrue();
        });
    }
}
