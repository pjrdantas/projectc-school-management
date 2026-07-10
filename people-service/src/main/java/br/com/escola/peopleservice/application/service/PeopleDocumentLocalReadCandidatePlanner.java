package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadCandidatePlan;

@Service
public class PeopleDocumentLocalReadCandidatePlanner {

    public PeopleDocumentLocalReadCandidatePlan planejarCandidatoDeLeituraLocalDeDocumento() {
        return new PeopleDocumentLocalReadCandidatePlan(
                "Fase 74",
                "people_document_local_metadata_adapter_diagnostic",
                "metadata_local_read_candidate_diagnostic_started_continue_document_family",
                "prepare_people_document_local_metadata_schema_diagnostic_without_route_or_upload_migration",
                "people_document_local_metadata_schema_diagnostic",
                true,
                true,
                true,
                false,
                false,
                List.of(
                        "pessoa_documento",
                        "documento",
                        "tipo_documento",
                        "pessoa"),
                Map.of(
                        "pessoa_documento", List.of(
                                "id_pessoa_documento",
                                "id_pessoa",
                                "id_documento",
                                "created_at"),
                        "documento", List.of(
                                "id_documento",
                                "id_tipo_documento",
                                "numero_documento",
                                "caminho_arquivo",
                                "observacao",
                                "data_upload"),
                        "tipo_documento", List.of(
                                "id_tipo_documento",
                                "codigo",
                                "descricao"),
                        "pessoa", List.of(
                                "id_pessoa",
                                "id_escola")),
                "pessoa_documento.id_pessoa_documento",
                List.of(
                        "pessoa_documento.id_pessoa",
                        "pessoa_documento.id_documento",
                        "documento.id_tipo_documento",
                        "documento.data_upload",
                        "pessoa.id_escola"),
                List.of(
                        "school scope must continue to be enforced by pessoa.id_escola",
                        "documento without pessoa link is invalid for this read model",
                        "aluno/responsavel resolution remains outside first local metadata adapter"),
                List.of(
                        "metadata read still depends on four-table join",
                        "delete remains coupled to orphan cleanup in monolith",
                        "binary file handling must stay out of local read candidate",
                        "tipo_documento drift blocks green reconciliation"),
                List.of(
                        "define physical read-model table names before adapter creation",
                        "define whether caminho_arquivo is stored full or normalized",
                        "prepare backfill only after reconciliation checks are fixed",
                        "keep monolith as source for upload and delete"),
                List.of(
                        "do-not-create-document-route-in-people-service",
                        "disable-any-future-document-local-adapter-if-divergence-appears",
                        "keep-document-metadata-served-by-monolith-proxy-until-schema-and-reconciliation-are-green"),
                List.of(
                        "upload-migration",
                        "document-delete-cutover",
                        "bff-route-change",
                        "frontend-change",
                        "local-binary-storage"));
    }
}
