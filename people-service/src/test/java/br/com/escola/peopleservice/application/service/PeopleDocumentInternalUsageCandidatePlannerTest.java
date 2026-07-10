package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentInternalUsageCandidatePlannerTest {

    @Test
    void deveDiagnosticarAusenciaDeConsumidorInternoSeguro() {
        var planner = new PeopleDocumentInternalUsageCandidatePlanner();

        var plan = planner.planejarUsoInternoMinimo();

        assertThat(plan.phase()).isEqualTo("Fase 78");
        assertThat(plan.slice()).isEqualTo("people_document_internal_usage_candidate_diagnostic");
        assertThat(plan.status()).isEqualTo("no_safe_internal_consumer_without_route_or_monolith_contract_change");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_78_and_start_funcionario_diagnostic_instead_of_forcing_document_usage");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_minimal_diagnostic");
        assertThat(plan.internalUsageCandidateFound()).isFalse();
        assertThat(plan.safeToConnectNow()).isFalse();
        assertThat(plan.externalRouteChangeRequired()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
    }
}
