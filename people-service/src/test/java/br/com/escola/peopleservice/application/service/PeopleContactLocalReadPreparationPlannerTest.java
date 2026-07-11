package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleContactLocalReadPreparationPlannerTest {

    @Test
    void devePlanejarLeituraLocalDeContatoSemRotaExterna() {
        PeopleContactLocalReadPreparationPlanner planner = new PeopleContactLocalReadPreparationPlanner();

        var plan = planner.planejarPreparacaoDeContatoLocal();

        assertThat(plan.phase()).isEqualTo("Fase 92");
        assertThat(plan.slice()).isEqualTo("people_contact_local_read_preparation");
        assertThat(plan.status()).isEqualTo("internal_contact_read_prepared_with_local_adapter_no_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_92_and_plan_people_contact_internal_consumer_diagnostic");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_contact_internal_consumer_diagnostic");
        assertThat(plan.contractPrepared()).isTrue();
        assertThat(plan.internalServicePrepared()).isTrue();
        assertThat(plan.adapterCreated()).isTrue();
        assertThat(plan.localPersistenceConnected()).isTrue();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("pessoa");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_proxy");
    }
}
