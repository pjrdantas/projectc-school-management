package br.com.escola.documento.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.documento.application.dto.DocumentoPeopleConsumerDiagnosticPlan;

@Service
public class DocumentoPeopleConsumerDiagnosticPlanner {

    public DocumentoPeopleConsumerDiagnosticPlan planejarPrimeiroConsumidorDeMetadataPeople() {
        return new DocumentoPeopleConsumerDiagnosticPlan(
                "Fase 87",
                "documento_people_first_consumer_diagnostic",
                "first_safe_consumer_is_documento_aluno_listar_por_aluno_without_route_change",
                "prepare_documento_aluno_metadata_consumer_contract_without_changing_upload_or_delete",
                "documento_aluno_metadata_consumer_contract",
                true,
                true,
                false,
                false,
                "DocumentoAlunoService.listarPorAluno",
                "GET /api/documentos-alunos/alunos/{alunoId}",
                "school-management-service:DocumentoGateway.findByEntidade",
                "people-service:PeopleDocumentMetadataLocalReadPort.listarDocumentosPorPessoa",
                List.of(
                        "listarPorAluno already scopes the flow to a single aggregate without generic entidadeTipo branching",
                        "the flow can reuse alunoId to resolve pessoaId before any future people-service call",
                        "the response is metadata-oriented and does not require upload or cleanup authority changes",
                        "buscarPorId is broader because it still needs to reconstruct entidadeTipo and entidadeId from documento ownership"),
                List.of(
                        "DocumentoAlunoService currently depends on ListarDocumentosPorEntidadeUseCase",
                        "DocumentoPersistenceGateway resolves pessoaId through AlunoMatriculaPort and then queries DocumentoJpaRepository",
                        "DocumentoJpaRepository still joins documento, pessoa_documento and pessoa inside the monolith"),
                List.of(
                        "future consumer must preserve school scoping by pessoa.id_escola",
                        "future mapping must keep tipo_documento metadata aligned with current response payload",
                        "fallback to current DocumentoGateway remains mandatory until people metadata read is proven green",
                        "alunoId to pessoaId resolution stays in the monolith boundary for this first step"),
                List.of(
                        "disable-future-documento-people-consumer-flag-if-created",
                        "route-listar-por-aluno-back-to-DocumentoGateway",
                        "keep-DocumentoJpaRepository-as-only-read-authority-for-documento-now"),
                List.of(
                        "keep POST /api/documentos-alunos on monolith current flow",
                        "keep DELETE /api/documentos-alunos/{id} on monolith current flow",
                        "keep generic /api/documentos multi-entidade flow untouched in this phase",
                        "keep people-service without new external route or upload authority"),
                List.of(
                        "document-upload-cutover",
                        "document-cleanup-cutover",
                        "generic-documento-route-refactor",
                        "bff-change",
                        "frontend-change",
                        "people-service-write-authority"));
    }
}
