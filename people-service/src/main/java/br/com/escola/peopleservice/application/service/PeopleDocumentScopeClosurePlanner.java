package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeClosurePlan.NextFamilyCandidate;

@Service
public class PeopleDocumentScopeClosurePlanner {

    public PeopleDocumentScopeClosurePlan planejarFechamentoEscopoPessoaDocumento() {
        return new PeopleDocumentScopeClosurePlan(
                "Fase 86",
                "people_document_metadata_scope_closure_review",
                "people_document_metadata_scope_review_closed_ready_for_next_family_diagnostic",
                "start_next_backend_family_without_reopening_people_document_metadata",
                "next_backend_family_diagnostic",
                true,
                false,
                false,
                true,
                List.of(
                        "people_documento scope diagnostic completed with ownership and cleanup boundaries preserved on monolith",
                        "internal metadata read contract prepared without exposing JPA entities",
                        "local read candidate, schema and JDBC adapter prepared for people_documento_read_model",
                        "backfill and reconciliation prepared without external route activation",
                        "guarded internal local read eligibility prepared with mandatory fallback to monolith_proxy",
                        "internal usage diagnostic completed without forcing an artificial consumer"),
                List.of(
                        "no justified internal consumer exists in people-service for document metadata at this stage",
                        "upload, listagem and cleanup flows remain in school-management-service",
                        "document binary and cleanup authority remain on monolith current flows",
                        "any external route or write activation would expand scope beyond the closed backend/backend block"),
                List.of(
                        new NextFamilyCandidate(
                                "next_backend_family",
                                "allowed_now",
                                true,
                                "document metadata block is closed for this stage and does not require reopening"),
                        new NextFamilyCandidate(
                                "people_document_read_cutover",
                                "not_allowed_now",
                                false,
                                "no internal consumer or external route justifies activating document metadata reads"),
                        new NextFamilyCandidate(
                                "document_upload_cleanup_cutover",
                                "out_of_current_scope",
                                false,
                                "would reopen document binary and cleanup authority without a new boundary reason")),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "keep-document-local-read-unconsumed-by-application-flows",
                        "keep-document-metadata-and-binary-authority-on-monolith"),
                List.of(
                        "create-document-external-route",
                        "connect-document-local-read-to-query-service",
                        "bff-route-change",
                        "frontend-change",
                        "document-upload-cutover",
                        "document-cleanup-cutover"));
    }
}
