package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentAlunoConsumerContractPlan;

@Service
public class PeopleDocumentAlunoConsumerContractPlanner {

    public PeopleDocumentAlunoConsumerContractPlan planejarContratoDoConsumidorAluno() {
        return new PeopleDocumentAlunoConsumerContractPlan(
                "Fase 88",
                "people_document_aluno_consumer_contract",
                "future_aluno_consumer_contract_prepared_without_route_or_legacy_change",
                "prepare_people_document_aluno_consumer_connection_strategy_in_people_service_only",
                "people_document_aluno_consumer_connection_strategy",
                "documento_aluno_listar_por_aluno",
                "listarDocumentosPorPessoa",
                "people_document_read_model_candidate",
                "monolith_proxy",
                true,
                true,
                false,
                false,
                false,
                true,
                List.of(
                        "alunoId",
                        "pessoaId",
                        "escolaId"),
                List.of(
                        "preserve documentoId, tipoDocumentoId, tipoDocumentoCodigo e tipoDocumentoDescricao",
                        "preserve numeroDocumento, observacao e dataUpload",
                        "preserve caminhoArquivo only as metadata reference, without binary migration",
                        "allow consumer-side filtering by pessoa/aluno without new external contract"),
                List.of(
                        "no route creation in people-service",
                        "no BFF or frontend change",
                        "no legacy controller/service/repository change",
                        "no binary storage migration",
                        "no upload, delete or cleanup authority migration"),
                List.of(
                        "keep future consumer disconnected from application flows",
                        "keep fallback source as monolith_proxy",
                        "discard contract if connection strategy finds hidden aluno to pessoa dependency"),
                List.of(
                        "connect-local-read-directly-to-legacy-flow-now",
                        "create-documento-aluno-route-now",
                        "change-consultarCadastro",
                        "bff-route-change",
                        "frontend-change",
                        "document-write-cutover"));
    }
}
