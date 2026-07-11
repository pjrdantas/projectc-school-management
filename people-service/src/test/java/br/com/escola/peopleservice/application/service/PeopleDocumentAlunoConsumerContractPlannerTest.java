package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleDocumentAlunoConsumerContractPlannerTest {

    @Test
    void devePlanejarContratoDoConsumidorAlunoDeDocumentos() {
        PeopleDocumentAlunoConsumerContractPlanner planner = new PeopleDocumentAlunoConsumerContractPlanner();

        var plan = planner.planejarContratoDoConsumidorAluno();

        assertThat(plan.phase()).isEqualTo("Fase 88");
        assertThat(plan.slice()).isEqualTo("people_document_aluno_consumer_contract");
        assertThat(plan.status()).isEqualTo("future_aluno_consumer_contract_prepared_without_route_or_legacy_change");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_people_document_aluno_consumer_connection_strategy_in_people_service_only");
        assertThat(plan.minimalNextSlice()).isEqualTo("people_document_aluno_consumer_connection_strategy");
        assertThat(plan.firstFutureConsumer()).isEqualTo("documento_aluno_listar_por_aluno");
        assertThat(plan.consumerOperation()).isEqualTo("listarDocumentosPorPessoa");
        assertThat(plan.candidateSource()).isEqualTo("people_document_read_model_candidate");
        assertThat(plan.fallbackSource()).isEqualTo("monolith_proxy");
        assertThat(plan.contractPrepared()).isTrue();
        assertThat(plan.localReadServiceReusable()).isTrue();
        assertThat(plan.safeToConnectNow()).isFalse();
        assertThat(plan.routeChangeRequiredNow()).isFalse();
        assertThat(plan.monolithChangeRequiredNow()).isFalse();
        assertThat(plan.fallbackRequired()).isTrue();
        assertThat(plan.consumerInputKeys()).containsExactly("alunoId", "pessoaId", "escolaId");
        assertThat(plan.preservedBoundaries()).contains(
                "no route creation in people-service",
                "no legacy controller/service/repository change");
    }
}
