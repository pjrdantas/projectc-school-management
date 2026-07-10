package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalUsageCandidatePlan;

@Service
public class PeopleDocumentInternalUsageCandidatePlanner {

    public PeopleDocumentInternalUsageCandidatePlan planejarUsoInternoMinimo() {
        return new PeopleDocumentInternalUsageCandidatePlan(
                "Fase 78",
                "people_document_internal_usage_candidate_diagnostic",
                "no_safe_internal_consumer_without_route_or_monolith_contract_change",
                "close_phase_78_and_start_funcionario_diagnostic_instead_of_forcing_document_usage",
                "funcionario_minimal_diagnostic",
                false,
                false,
                false,
                true,
                List.of(
                        "people-service has no native internal document workflow beyond observability and local adapter validation",
                        "current real document flows remain inside school-management-service",
                        "connecting PessoaQueryService would change external query scope and is out of phase boundary",
                        "creating a new internal REST route now would add surface area before a justified consumer exists"),
                List.of(
                        "no new route in people-service",
                        "no query-service connection",
                        "no consultarCadastro change",
                        "no BFF/frontend change",
                        "no document write cutover"),
                List.of(
                        "keep-document-local-read-service-unconsumed-by-application-flows",
                        "keep-read-model-cutover-guard-and-fallback-mandatory",
                        "prefer-next-family-diagnostic-over-artificial-document-consumer"));
    }
}
