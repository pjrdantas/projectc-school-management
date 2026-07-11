package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleStudentResponsibleLinkScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleStudentResponsibleLinkScopeClosurePlan.NextFamilyCandidate;

@Service
public class PeopleStudentResponsibleLinkScopeClosurePlanner {

    public PeopleStudentResponsibleLinkScopeClosurePlan planejarFechamentoEscopoVinculosAlunoResponsavel() {
        return new PeopleStudentResponsibleLinkScopeClosurePlan(
                "Fase 96",
                "student_responsible_link_scope_closure_review",
                "student_responsible_link_scope_review_closed_ready_for_next_family_diagnostic",
                "start_next_backend_family_without_reopening_student_responsible_links",
                "next_backend_family_diagnostic",
                true,
                false,
                false,
                true,
                List.of(
                        "student and responsible pessoa lookup contracts prepared without exposing JPA entities",
                        "local lookup services and JDBC adapters prepared over current people read model",
                        "document consumer strategies reuse these lookups without forcing new routes",
                        "student responsible diagnostics available in actuator without external route activation"),
                List.of(
                        "parentesco remains outside this family and still has no dedicated contract in people-service",
                        "status_aluno remains outside this family and still has no dedicated contract in people-service",
                        "any external route or query-service expansion would enlarge scope beyond the closed backend/backend block"),
                List.of(
                        new NextFamilyCandidate(
                                "next_backend_family",
                                "allowed_now",
                                true,
                                "student and responsible base link family is closed for this stage"),
                        new NextFamilyCandidate(
                                "student_responsible_external_connection",
                                "not_allowed_now",
                                false,
                                "would expand external contracts without a justified new consumer"),
                        new NextFamilyCandidate(
                                "parentesco_or_status_catalogs",
                                "separate_future_family",
                                false,
                                "requires a distinct family because it is broader than the current link closure")),
                List.of(
                        "keep-student-and-responsible-lookups-internal-only",
                        "keep-student-responsible-write-authority-on-monolith",
                        "keep-parentesco-and-status-aluno-outside-current-family"),
                List.of(
                        "create-student-or-responsible-external-route",
                        "connect-lookups-to-new-query-service-contracts",
                        "consultarCadastro-payload-change",
                        "bff-route-change",
                        "frontend-change",
                        "student-or-responsible-write-cutover"));
    }
}
