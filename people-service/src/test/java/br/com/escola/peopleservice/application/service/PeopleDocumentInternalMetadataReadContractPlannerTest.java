package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentInternalMetadataReadContractPlannerTest {

    @Test
    void devePlanejarContratoInternoMinimoSemAdapterNemRota() {
        PeopleDocumentInternalMetadataReadContractPlanner planner =
                new PeopleDocumentInternalMetadataReadContractPlanner();

        var plan = planner.planejarContratoInternoDeLeituraDeMetadados();

        assertThat(plan.phase()).isEqualTo("Fase 73");
        assertThat(plan.slice()).isEqualTo("people_document_internal_metadata_read_contract");
        assertThat(plan.status()).isEqualTo("internal_contract_prepared_no_adapter_no_route");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_73_and_plan_people_document_local_adapter_diagnostic");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_local_metadata_adapter_diagnostic");
        assertThat(plan.contractPrepared()).isTrue();
        assertThat(plan.internalServicePrepared()).isTrue();
        assertThat(plan.adapterCreated()).isFalse();
        assertThat(plan.localPersistenceConnected()).isFalse();
        assertThat(plan.externalRouteCreated()).isFalse();
        assertThat(plan.candidateSource()).isEqualTo("people_document_read_model_candidate");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_proxy");
    }
}
