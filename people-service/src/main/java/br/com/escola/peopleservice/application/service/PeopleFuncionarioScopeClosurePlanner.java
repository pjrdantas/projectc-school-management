package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeClosurePlan.NextFamilyCandidate;

@Service
public class PeopleFuncionarioScopeClosurePlanner {

    public PeopleFuncionarioScopeClosurePlan planejarFechamentoEscopoFuncionario() {
        return new PeopleFuncionarioScopeClosurePlan(
                "Fase 85",
                "funcionario_internal_summary_scope_closure_review",
                "funcionario_internal_summary_scope_review_closed_ready_for_next_family_diagnostic",
                "start_next_backend_family_without_reopening_funcionario_internal_summary",
                "next_backend_family_diagnostic",
                true,
                false,
                false,
                true,
                List.of(
                        "funcionario scope diagnostic completed with professor and auth preserved on monolith",
                        "internal summary contract prepared without exposing JPA entities",
                        "JDBC adapter and read model migration prepared for people_funcionario_read_model",
                        "backfill and reconciliation prepared without external route activation",
                        "guarded internal local read eligibility prepared with mandatory fallback to monolith_internal_rh",
                        "internal usage diagnostic completed without forcing an artificial consumer"),
                List.of(
                        "no justified internal consumer exists in people-service for funcionario summary at this stage",
                        "professor orchestration remains in school-management-service and academic-professor-service",
                        "RH authority and auth dependencies remain on monolith",
                        "any external route or write activation would expand scope beyond the closed backend/backend block"),
                List.of(
                        new NextFamilyCandidate(
                                "next_backend_family",
                                "allowed_now",
                                true,
                                "funcionario internal summary block is closed for this stage and does not require reopening"),
                        new NextFamilyCandidate(
                                "funcionario_write_cutover",
                                "not_allowed_now",
                                false,
                                "write authority remains on monolith and no consumer justifies activation"),
                        new NextFamilyCandidate(
                                "auth_or_professor_recut",
                                "out_of_current_scope",
                                false,
                                "would reopen already isolated families without a new boundary reason")),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "keep-funcionario-local-read-unconsumed-by-application-flows",
                        "keep-rh-and-auth-authority-on-monolith"),
                List.of(
                        "create-funcionario-external-route",
                        "connect-funcionario-local-read-to-query-service",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "auth-cutover"));
    }
}
