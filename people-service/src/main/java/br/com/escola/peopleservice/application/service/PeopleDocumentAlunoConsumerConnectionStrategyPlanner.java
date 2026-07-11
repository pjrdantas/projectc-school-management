package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentAlunoConsumerConnectionStrategyPlan;

@Service
public class PeopleDocumentAlunoConsumerConnectionStrategyPlanner {

    public PeopleDocumentAlunoConsumerConnectionStrategyPlan planejarEstrategiaDeConexao() {
        return new PeopleDocumentAlunoConsumerConnectionStrategyPlan(
                "Fase 89",
                "people_document_aluno_consumer_connection_strategy",
                "aluno_pessoa_local_resolution_prepared_without_real_consumer_connection",
                "close_phase_89_and_keep_consumer_unconnected_until_a_new_service_flow_justifies_it",
                "people_document_aluno_consumer_connection_closure",
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                List.of(
                        "PeopleStudentPessoaLocalReadPort",
                        "PeopleStudentPessoaLocalReadService",
                        "JdbcPeopleStudentPessoaLocalReadAdapter",
                        "PessoaAlunoVinculoResponse"),
                List.of(
                        "receive alunoId and escolaId from future consumer flow",
                        "resolve pessoaId by alunoId inside people-service local read model",
                        "call PeopleDocumentMetadataLocalReadService.listarDocumentosPorPessoa with resolved pessoaId",
                        "keep fallback semantics at consumer integration boundary, not inside current phase"),
                List.of(
                        "no route creation in people-service",
                        "no current application flow connection",
                        "no BFF or frontend change",
                        "no school-management-service change",
                        "no document binary migration"),
                List.of(
                        "disconnect future consumer from local resolution service if a later phase finds hidden dependency",
                        "keep document consumer unconnected to real flow",
                        "preserve monolith_proxy as fallback source at future integration boundary"),
                List.of(
                        "create-document-route-now",
                        "connect-to-real-consumer-now",
                        "change-consultarCadastro-response",
                        "bff-route-change",
                        "frontend-change",
                        "legacy-refactor"));
    }
}
