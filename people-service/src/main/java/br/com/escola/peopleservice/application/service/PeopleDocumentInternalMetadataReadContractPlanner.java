package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalMetadataReadContractPlan;

@Service
public class PeopleDocumentInternalMetadataReadContractPlanner {

    public PeopleDocumentInternalMetadataReadContractPlan planejarContratoInternoDeLeituraDeMetadados() {
        return new PeopleDocumentInternalMetadataReadContractPlan(
                "Fase 73",
                "people_document_internal_metadata_read_contract",
                "internal_contract_prepared_no_adapter_no_route",
                "close_phase_73_and_plan_people_document_local_adapter_diagnostic",
                "people_document_local_metadata_adapter_diagnostic",
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                "people_document_read_model_candidate",
                "monolith_proxy",
                true,
                List.of(
                        "id_pessoa_documento",
                        "id_pessoa",
                        "id_documento",
                        "id_tipo_documento",
                        "tipo_documento_codigo",
                        "tipo_documento_descricao",
                        "numero_documento",
                        "caminho_arquivo",
                        "observacao",
                        "data_upload"),
                List.of(
                        "listarDocumentosPorPessoa",
                        "buscarDocumentoPorId"),
                List.of(
                        "sem entidade JPA no contrato interno",
                        "sem rota externa",
                        "sem BFF e sem frontend",
                        "sem adapter JDBC nesta subfase",
                        "sem mover upload, exclusao ou cleanup do monolito"),
                List.of(
                        "manter somente monolith_proxy para documentos",
                        "nao conectar adapter local enquanto nao houver schema e reconciliacao",
                        "nao ativar rota externa de documentos no people-service"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "local-document-write-authority",
                        "binary-storage-migration"));
    }
}
