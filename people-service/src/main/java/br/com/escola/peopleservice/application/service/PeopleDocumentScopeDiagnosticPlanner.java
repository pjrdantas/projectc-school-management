package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeDiagnosticPlan;

@Service
public class PeopleDocumentScopeDiagnosticPlanner {

    public PeopleDocumentScopeDiagnosticPlan planejarDiagnosticoEscopoPessoaDocumento() {
        return new PeopleDocumentScopeDiagnosticPlan(
                "Fase 73",
                "people_document_contract_diagnostic",
                "document_metadata_read_contract_preferred_write_cleanup_stays_on_monolith",
                "prepare_internal_people_document_metadata_read_contract_without_bff_or_write_cutover",
                "people_document_internal_metadata_read_contract",
                true,
                true,
                false,
                false,
                true,
                List.of(
                        "listar metadados de documentos por pessoa para aluno/responsavel sem mover upload",
                        "buscar documento por id apenas para metadata e ownership por escola",
                        "manter download/arquivo fisico fora deste primeiro recorte"),
                List.of(
                        "upload de documento continua no monolito por acoplamento com arquivo e tipo_documento",
                        "exclusao de documento continua no monolito por cleanup em pessoa_documento",
                        "cleanup por exclusao de aluno/responsavel continua no monolito"),
                List.of(
                        "DocumentoController.GET /api/documentos e GET /api/documentos/{id}",
                        "DocumentoAlunoController.GET /api/documentos-alunos/alunos/{alunoId}",
                        "DocumentoPersistenceGateway.findByEntidade/findById/deleteByEntidade",
                        "DocumentoJpaRepository JOIN pessoa_documento + pessoa + documento",
                        "AlunoPersistenceGateway.deleteById e ResponsavelPersistenceGateway.deleteById"),
                List.of(
                        "pessoa_documento nao e suficiente sozinho; leitura depende de documento e ownership por escola",
                        "delete precisa remover vinculos e depois documentos orfaos para evitar lixo",
                        "tipo_documento continua como catalogo obrigatorio para resposta funcional",
                        "qualquer cutover de leitura futura exige reconciliacao entre pessoa_documento e documento"),
                List.of(
                        "numeroDocumento e caminhoArquivo sao dados sensiveis e nao devem ampliar exposicao nesta fase",
                        "ownership multi-escola depende do join com pessoa.id_escola, nao apenas do documento",
                        "metadados minimos devem evitar antecipar download binario ou storage externo"),
                List.of(
                        "se houver leitura local futura, migrar primeiro metadados minimos de documento e vinculo pessoa_documento",
                        "nao criar migration de escrita nem storage de arquivo nesta subfase",
                        "preservar chave de reconciliacao por id_documento e id_pessoa antes de qualquer leitura local"),
                List.of(
                        "manter rotas de documento no monolito",
                        "desligar qualquer adapter interno novo e voltar a consultar somente o monolito",
                        "nao mover upload/exclusao/cleanup para people-service nesta macrofase"),
                List.of(
                        "primeira implementacao deve ser read-only e interna",
                        "contrato deve trafegar apenas metadata sem entidade JPA",
                        "sem rota externa nova, sem BFF e sem frontend",
                        "sem persistencia autoritativa local de documento ou arquivo"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "local-authoritative-document-write",
                        "binary-file-storage-migration",
                        "reactivate-address-scope"));
    }
}
