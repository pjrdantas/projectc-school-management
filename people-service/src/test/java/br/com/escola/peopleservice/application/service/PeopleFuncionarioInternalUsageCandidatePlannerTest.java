package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleFuncionarioInternalUsageCandidatePlannerTest {

    @Test
    void deveDiagnosticarAusenciaDeConsumidorInternoSeguroDeFuncionario() {
        var planner = new PeopleFuncionarioInternalUsageCandidatePlanner();

        var plan = planner.planejarUsoInternoMinimo();

        assertThat(plan.phase()).isEqualTo("Fase 84");
        assertThat(plan.slice()).isEqualTo("funcionario_internal_summary_internal_usage_candidate_diagnostic");
        assertThat(plan.status()).isEqualTo("no_safe_internal_funcionario_consumer_without_route_or_monolith_contract_change");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_84_and_keep_funcionario_internal_summary_prepared_without_forced_consumer");
        assertThat(plan.minimalNextSlice()).isEqualTo("funcionario_internal_summary_block_closure");
        assertThat(plan.internalUsageCandidateFound()).isFalse();
        assertThat(plan.safeToConnectNow()).isFalse();
        assertThat(plan.externalRouteChangeRequired()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
    }
}
