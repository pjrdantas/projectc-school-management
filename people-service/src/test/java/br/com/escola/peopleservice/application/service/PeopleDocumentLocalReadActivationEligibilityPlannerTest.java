package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentLocalReadActivationEligibilityPlannerTest {

    @Test
    void devePlanejarElegibilidadeDeAtivacaoInternaDeDocumento() {
        var planner = new PeopleDocumentLocalReadActivationEligibilityPlanner();

        var plan = planner.planejarElegibilidadeDeAtivacao();

        assertThat(plan.phase()).isEqualTo("Fase 77");
        assertThat(plan.slice()).isEqualTo("people_document_local_read_activation_eligibility");
        assertThat(plan.status()).isEqualTo("internal_document_local_read_guarded_without_external_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_77_and_only_consider_internal_document_usage_when_guard_is_green");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_internal_usage_candidate");
        assertThat(plan.internalServiceConnected()).isTrue();
        assertThat(plan.localReadGuardPrepared()).isTrue();
        assertThat(plan.localReadCutoverAllowedNow()).isFalse();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
        assertThat(plan.routingOperation()).isEqualTo("documentMetadataLocalRead");
        assertThat(plan.candidateSource()).isEqualTo("people_documento_read_model");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_proxy");
    }
}
