package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleStudentResponsibleLinkScopeClosurePlannerTest {

    @Test
    void devePlanejarFechamentoDoEscopoDeVinculosAlunoResponsavel() {
        PeopleStudentResponsibleLinkScopeClosurePlanner planner =
                new PeopleStudentResponsibleLinkScopeClosurePlanner();

        var plan = planner.planejarFechamentoEscopoVinculosAlunoResponsavel();

        assertThat(plan.phase()).isEqualTo("Fase 96");
        assertThat(plan.slice()).isEqualTo("student_responsible_link_scope_closure_review");
        assertThat(plan.status())
                .isEqualTo("student_responsible_link_scope_review_closed_ready_for_next_family_diagnostic");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("start_next_backend_family_without_reopening_student_responsible_links");
        assertThat(plan.minimalNextSlice()).isEqualTo("next_backend_family_diagnostic");
        assertThat(plan.readScopeClosed()).isTrue();
        assertThat(plan.writeScopePreparedWithoutCutover()).isFalse();
        assertThat(plan.activationRequiredNow()).isFalse();
        assertThat(plan.safeToStartNextFamilyDiagnostic()).isTrue();
        assertThat(plan.closedCapabilities()).contains(
                "student and responsible pessoa lookup contracts prepared without exposing JPA entities",
                "local lookup services and JDBC adapters prepared over current people read model");
        assertThat(plan.remainingActivationBlockers()).contains(
                "parentesco remains outside this family and still has no dedicated contract in people-service");
        assertThat(plan.nextFamilyCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.family()).isEqualTo("next_backend_family");
            assertThat(candidate.status()).isEqualTo("allowed_now");
            assertThat(candidate.allowedNow()).isTrue();
        });
    }
}
