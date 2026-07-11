package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleContactInternalUsageCandidatePlannerTest {

    @Test
    void deveDiagnosticarAusenciaDeConsumidorInternoSeguroParaContato() {
        var planner = new PeopleContactInternalUsageCandidatePlanner();

        var plan = planner.planejarUsoInternoMinimo();

        assertThat(plan.phase()).isEqualTo("Fase 93");
        assertThat(plan.slice()).isEqualTo("people_contact_internal_usage_candidate_diagnostic");
        assertThat(plan.status()).isEqualTo("no_safe_internal_contact_consumer_without_route_or_query_scope_change");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_93_and_keep_contact_prepared_without_forced_consumer");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_contact_block_closure");
        assertThat(plan.internalUsageCandidateFound()).isFalse();
        assertThat(plan.safeToConnectNow()).isFalse();
        assertThat(plan.externalRouteChangeRequired()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
    }
}
