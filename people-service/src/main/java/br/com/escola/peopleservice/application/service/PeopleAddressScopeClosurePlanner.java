package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleAddressScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressScopeClosurePlan.NextFamilyCandidate;

@Service
public class PeopleAddressScopeClosurePlanner {

    public PeopleAddressScopeClosurePlan planejarFechamentoEscopoPessoaEndereco() {
        return new PeopleAddressScopeClosurePlan(
                "Fase 73",
                "people_address_scope_closure_review",
                "people_address_scope_review_closed_ready_for_next_family_diagnostic",
                "start_people_document_scope_diagnostic_without_reopening_address_cutover",
                "people_document_contract_diagnostic",
                true,
                true,
                false,
                true,
                List.of(
                        "catalog local read-only schema closed for tipo_pessoa and tipo_endereco",
                        "identity local read closed for pessoa and pessoa_tipo_pessoa behind guarded fallback",
                        "consultarCadastro local read cutover closed with mandatory fallback",
                        "address local read contract, JDBC adapter and guarded internal service prepared",
                        "address local read cutover eligibility diagnosed without external exposure",
                        "address shadow write contract prepared without JPA entities",
                        "monolith internal HTTP write contract for address available",
                        "MonolithPessoaAddressWriteClient implemented behind disabled-by-default guard"),
                List.of(
                        "create-person-with-address remains inside monolith person transaction",
                        "people-service local read model is not authoritative write storage",
                        "no external people-service write route should be activated for address",
                        "address write adapter guard must remain disabled until a future explicit activation phase",
                        "funcionario, professor and pessoa_documento are still outside current people-service scope"),
                List.of(
                        new NextFamilyCandidate(
                                "pessoa_documento",
                                "next_minimal_candidate",
                                true,
                                "extends pessoa identity without reopening address authority"),
                        new NextFamilyCandidate(
                                "funcionario",
                                "candidate_after_people_document",
                                false,
                                "depends on role and school employment rules beyond current closure"),
                        new NextFamilyCandidate(
                                "professor",
                                "out_of_current_people_service_scope",
                                false,
                                "already handled in academic-professor-service and should not be reopened here")),
                List.of(
                        "disable-people.shadow.monolith.address-write-adapter-enabled",
                        "keep-address-shadow-service-returning-monolith_proxy",
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled-if-any-risk-appears",
                        "keep-address-and-person-authority-on-monolith-for-non-closed-families"),
                List.of(
                        "activate-address-write-cutover",
                        "create-people-service-address-write-route",
                        "frontend-change",
                        "bff-route-change",
                        "local-authoritative-address-persistence",
                        "move-create-person-with-address-out-of-monolith"));
    }
}
