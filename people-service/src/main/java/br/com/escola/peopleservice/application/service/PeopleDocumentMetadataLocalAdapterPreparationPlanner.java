package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataLocalAdapterPreparationPlan;

@Service
public class PeopleDocumentMetadataLocalAdapterPreparationPlanner {

    public PeopleDocumentMetadataLocalAdapterPreparationPlan planejarPreparacaoDoAdapterLocal() {
        return new PeopleDocumentMetadataLocalAdapterPreparationPlan(
                "Fase 75",
                "people_document_local_metadata_adapter_preparation",
                "jdbc_local_adapter_prepared_internal_fallback_only",
                "close_phase_75_and_plan_people_document_backfill_reconciliation_preparation",
                "people_document_backfill_reconciliation_preparation",
                true,
                true,
                true,
                false,
                false,
                "people_documento_read_model",
                "monolith_proxy",
                "documentMetadataLocalRead",
                "V5__create_people_document_metadata_read_model.sql",
                Map.of(
                        "port", "PeopleDocumentMetadataLocalReadPort",
                        "response", "PessoaDocumentoMetadataLocalReadResponse",
                        "adapter", "JdbcPeopleDocumentMetadataLocalReadAdapter",
                        "internalService", "PeopleDocumentMetadataLocalReadService",
                        "adapterCreated", true,
                        "internalServiceConnected", true,
                        "migrationCreated", true,
                        "routeCreated", false,
                        "queryServiceConnected", false),
                List.of(
                        "schema-migration-must-remain-opt-in",
                        "backfill-and-reconciliation-must-be-green-before-any-activation",
                        "caminho_arquivo-normalization-must-stay-defined-before-runtime-use",
                        "document-ownership-by-id_escola-must-be-kept-consistent"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.migration-enabled"),
                List.of(
                        "create-document-route-in-people-service",
                        "bff-route-change",
                        "frontend-change",
                        "upload-migration",
                        "document-delete-cutover",
                        "local-binary-storage"));
    }
}
