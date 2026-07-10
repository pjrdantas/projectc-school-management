package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadActivationEligibilityPlan;

@Service
public class PeopleDocumentLocalReadActivationEligibilityPlanner {

    public PeopleDocumentLocalReadActivationEligibilityPlan planejarElegibilidadeDeAtivacao() {
        return new PeopleDocumentLocalReadActivationEligibilityPlan(
                "Fase 77",
                "people_document_local_read_activation_eligibility",
                "internal_document_local_read_guarded_without_external_route",
                "close_phase_77_and_only_consider_internal_document_usage_when_guard_is_green",
                "people_document_internal_usage_candidate",
                true,
                true,
                false,
                false,
                true,
                "documentMetadataLocalRead",
                "people_documento_read_model",
                "monolith_proxy",
                List.of(
                        "people.shadow.local-persistence.enabled=true",
                        "people.shadow.local-persistence.read-model-cutover-enabled=true",
                        "people.shadow.local-persistence.backfill-enabled=true",
                        "people.shadow.local-persistence.reconciliation-enabled=true",
                        "people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "localReadModelBackfill.status=completed",
                        "localReadModelBackfill.divergences=0",
                        "document-metadata-source-has-no-duplicate-document-id",
                        "document-metadata-read-model-has-no-school-ownership-divergence"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "keep-document-read-on-monolith-proxy-or-empty-internal-fallback",
                        "rerun-document-backfill-and-reconciliation-before-any-internal-activation"),
                List.of(
                        "new-internal-rest-route",
                        "query-service-connection",
                        "consultarCadastro-change",
                        "bff-route-change",
                        "frontend-change",
                        "document-write-cutover"));
    }
}
