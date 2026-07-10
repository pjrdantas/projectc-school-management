package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataSchemaDiagnosticPlan;

@Service
public class PeopleDocumentMetadataSchemaDiagnosticPlanner {

    public PeopleDocumentMetadataSchemaDiagnosticPlan planejarSchemaMinimoDeMetadados() {
        return new PeopleDocumentMetadataSchemaDiagnosticPlan(
                "Fase 74",
                "people_document_local_metadata_schema_diagnostic",
                "schema_backfill_reconciliation_diagnosed_adapter_still_blocked",
                "close_phase_74_and_plan_people_document_local_adapter_preparation",
                "people_document_local_metadata_adapter_preparation",
                true,
                true,
                false,
                false,
                "people_documento_read_model.id_pessoa_documento",
                "phase_74_closure_no_local_adapter",
                Map.of(
                        "people_documento_read_model", List.of(
                                "id_pessoa_documento",
                                "id_pessoa",
                                "id_documento",
                                "id_tipo_documento",
                                "tipo_documento_codigo",
                                "tipo_documento_descricao",
                                "numero_documento",
                                "caminho_arquivo",
                                "observacao",
                                "data_upload",
                                "id_escola",
                                "created_at")),
                Map.of(
                        "version", "V5__create_people_document_metadata_read_model.sql",
                        "enabledByDefault", false,
                        "automaticBackfill", false),
                Map.of(
                        "enabledByDefault", false,
                        "source", "monolith_jdbc",
                        "target", "people_documento_read_model"),
                "document ownership remains valid only when id_escola is copied from pessoa and kept aligned",
                "caminho_arquivo must be copied as metadata only; no binary ownership transfer is allowed",
                List.of(
                        "id_pessoa",
                        "id_documento",
                        "id_tipo_documento",
                        "tipo_documento_codigo",
                        "numero_documento",
                        "caminho_arquivo",
                        "data_upload",
                        "id_escola"),
                List.of(
                        "tipo_documento code drift must be zero before adapter",
                        "caminho_arquivo normalization must be defined before adapter",
                        "orphaned documento rows without pessoa_documento must stay outside local read model",
                        "ownership divergence by pessoa.id_escola blocks green reconciliation"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.migration-enabled"),
                List.of(
                        "create-document-route-in-people-service",
                        "upload-migration",
                        "document-delete-cutover",
                        "bff-route-change",
                        "frontend-change",
                        "local-binary-storage"));
    }
}
