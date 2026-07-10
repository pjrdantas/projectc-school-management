package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalUsageCandidatePlan;

@Service
public class PeopleFuncionarioInternalUsageCandidatePlanner {

    public PeopleFuncionarioInternalUsageCandidatePlan planejarUsoInternoMinimo() {
        return new PeopleFuncionarioInternalUsageCandidatePlan(
                "Fase 84",
                "funcionario_internal_summary_internal_usage_candidate_diagnostic",
                "no_safe_internal_funcionario_consumer_without_route_or_monolith_contract_change",
                "close_phase_84_and_keep_funcionario_internal_summary_prepared_without_forced_consumer",
                "funcionario_internal_summary_block_closure",
                false,
                false,
                false,
                true,
                List.of(
                        "people-service has no native internal workflow that currently requires funcionario summary beyond guarded adapter validation",
                        "real professor, RH and auth orchestration still remain inside school-management-service",
                        "connecting PessoaQueryService or creating a new route would expand scope outside the current backend-only boundary",
                        "forcing an artificial consumer now would hide the absence of a justified runtime dependency"),
                List.of(
                        "no new route in people-service",
                        "no query-service connection",
                        "no BFF/frontend change",
                        "no funcionario write cutover",
                        "no auth cutover"),
                List.of(
                        "keep-funcionario-local-read-service-unconsumed-by-application-flows",
                        "keep-read-model-cutover-guard-and-fallback-mandatory",
                        "prefer-block-closure-over-artificial-internal-consumer"));
    }
}
