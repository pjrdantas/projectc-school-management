package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleContactScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleContactScopeClosurePlan.NextFamilyCandidate;

@Service
public class PeopleContactScopeClosurePlanner {

    public PeopleContactScopeClosurePlan planejarFechamentoEscopoContato() {
        return new PeopleContactScopeClosurePlan(
                "Fase 94",
                "people_contact_scope_closure_review",
                "people_contact_scope_review_closed_ready_for_next_family_diagnostic",
                "start_next_backend_family_without_reopening_people_contact",
                "next_backend_family_diagnostic",
                true,
                false,
                false,
                true,
                List.of(
                        "contact internal read contract prepared without exposing JPA entities",
                        "local read service and JDBC adapter prepared over pessoa read model",
                        "contact internal usage diagnostic completed without forcing an artificial consumer",
                        "contact metrics and health diagnostics available without external route activation"),
                List.of(
                        "no justified internal consumer exists in people-service for contact at this stage",
                        "aluno and responsavel contact presentation remain on current monolith flows",
                        "any external route or query-service connection would expand scope beyond the closed backend/backend block"),
                List.of(
                        new NextFamilyCandidate(
                                "next_backend_family",
                                "allowed_now",
                                true,
                                "contact block is closed for this stage and does not require reopening"),
                        new NextFamilyCandidate(
                                "contact_query_connection",
                                "not_allowed_now",
                                false,
                                "would expand PessoaQueryService or external contracts without a justified consumer"),
                        new NextFamilyCandidate(
                                "contact_write_cutover",
                                "out_of_current_scope",
                                false,
                                "write authority for pessoa contact data remains on monolith")),
                List.of(
                        "keep-contact-local-read-unconsumed-by-application-flows",
                        "keep-contact-read-without-external-route",
                        "keep-pessoa-contact-write-authority-on-monolith"),
                List.of(
                        "create-contact-external-route",
                        "connect-contact-local-read-to-query-service",
                        "consultarCadastro-payload-change",
                        "bff-route-change",
                        "frontend-change",
                        "contact-write-cutover"));
    }
}
