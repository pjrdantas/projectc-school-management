package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleContactInternalUsageCandidatePlan;

@Service
public class PeopleContactInternalUsageCandidatePlanner {

    public PeopleContactInternalUsageCandidatePlan planejarUsoInternoMinimo() {
        return new PeopleContactInternalUsageCandidatePlan(
                "Fase 93",
                "people_contact_internal_usage_candidate_diagnostic",
                "no_safe_internal_contact_consumer_without_route_or_query_scope_change",
                "close_phase_93_and_keep_contact_prepared_without_forced_consumer",
                "people_contact_block_closure",
                false,
                false,
                false,
                true,
                List.of(
                        "people-service has no native internal workflow that currently requires isolated contact reads beyond adapter validation",
                        "connecting PessoaQueryService would expand consultarCadastro or buscarPorId scope in the current phase",
                        "real aluno and responsavel contact presentation still belongs to current monolith flows",
                        "forcing an artificial consumer now would hide the absence of a justified runtime dependency"),
                List.of(
                        "no new route in people-service",
                        "no query-service connection",
                        "no consultarCadastro change",
                        "no BFF/frontend change",
                        "no contact write cutover"),
                List.of(
                        "keep-contact-local-read-service-unconsumed-by-application-flows",
                        "keep-contact-read-available-for-future-new-service-consumers-only",
                        "prefer-block-closure-over-artificial-internal-consumer"));
    }
}
