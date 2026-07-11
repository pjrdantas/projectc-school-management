package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleContactLocalReadPreparationPlan;

@Service
public class PeopleContactLocalReadPreparationPlanner {

    public PeopleContactLocalReadPreparationPlan planejarPreparacaoDeContatoLocal() {
        return new PeopleContactLocalReadPreparationPlan(
                "Fase 92",
                "people_contact_local_read_preparation",
                "internal_contact_read_prepared_with_local_adapter_no_route",
                "close_phase_92_and_plan_people_contact_internal_consumer_diagnostic",
                "people_contact_internal_consumer_diagnostic",
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                "pessoa",
                "monolith_proxy",
                true,
                List.of(
                        "id_pessoa",
                        "id_escola",
                        "email",
                        "telefone",
                        "ativo"),
                List.of(
                        "future-student-contact-read",
                        "future-responsible-contact-read",
                        "future-school-history-contact-reuse"),
                List.of(
                        "sem entidade JPA no contrato interno",
                        "sem rota externa",
                        "sem BFF e sem frontend",
                        "sem mover escrita de email ou telefone do monolito",
                        "sem alterar consultarCadastro nesta subfase"),
                List.of(
                        "keep-contact-read-inside-people-service-only",
                        "keep-monolith-as-functional-fallback-for-any-future-consumer",
                        "do-not-connect-contact-read-to-external-query-route"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "contact-write-cutover",
                        "consultarCadastro-payload-change"));
    }
}
