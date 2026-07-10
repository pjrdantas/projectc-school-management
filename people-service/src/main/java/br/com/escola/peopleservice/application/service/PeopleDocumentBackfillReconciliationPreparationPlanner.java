package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentBackfillReconciliationPreparationPlan;

@Service
public class PeopleDocumentBackfillReconciliationPreparationPlanner {

    public PeopleDocumentBackfillReconciliationPreparationPlan planejarBackfillReconciliacao() {
        return new PeopleDocumentBackfillReconciliationPreparationPlan(
                "Fase 76",
                "people_document_backfill_reconciliation_preparation",
                "document_metadata_backfill_reconciliation_prepared_no_read_cutover",
                "close_phase_76_and_keep_document_local_read_blocked_until_green",
                "people_document_local_read_activation_eligibility",
                true,
                true,
                true,
                false,
                "monolith_jdbc",
                "people_documento_read_model",
                "people_documento_read_model.id_pessoa_documento",
                List.of(
                        "pessoa_documento",
                        "documento",
                        "tipo_documento",
                        "pessoa"),
                List.of(
                        "duplicate-document-id-in-source-blocks-green-reconciliation",
                        "tipo_documento-drift-blocks-green-reconciliation",
                        "caminho_arquivo-normalization-must-remain-stable-before-activation",
                        "ownership-by-pessoa.id_escola-must-match-target"),
                List.of(
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled"),
                List.of(
                        "external-route-change",
                        "bff-route-change",
                        "frontend-change",
                        "upload-migration",
                        "document-delete-cutover",
                        "local-binary-storage"));
    }
}
