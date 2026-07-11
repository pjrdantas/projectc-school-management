package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentResponsavelConsumerConnectionStrategyPlannerTest {

    @Test
    void devePlanejarEstrategiaDeConexaoDoConsumidorResponsavel() {
        var planner = new PeopleDocumentResponsavelConsumerConnectionStrategyPlanner();

        var plan = planner.planejarEstrategiaDeConexao();

        assertThat(plan.phase()).isEqualTo("Fase 91");
        assertThat(plan.slice()).isEqualTo("people_document_responsavel_consumer_connection_strategy");
        assertThat(plan.status()).isEqualTo("responsavel_pessoa_local_resolution_prepared_without_real_consumer_connection");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("close_phase_91_and_keep_responsavel_consumer_unconnected_until_a_new_service_flow_justifies_it");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_responsavel_consumer_connection_closure");
        assertThat(plan.responsavelPessoaLookupContractPrepared()).isTrue();
        assertThat(plan.responsavelPessoaLookupAdapterPrepared()).isTrue();
        assertThat(plan.localResolutionReadyForConnection()).isTrue();
        assertThat(plan.realConsumerConnected()).isFalse();
        assertThat(plan.routeChangeRequiredNow()).isFalse();
        assertThat(plan.legacyChangeRequiredNow()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
        assertThat(plan.preparedArtifacts()).contains(
                "PeopleResponsiblePessoaLocalReadPort",
                "PeopleResponsiblePessoaLocalReadService",
                "JdbcPeopleResponsiblePessoaLocalReadAdapter");
    }
}
