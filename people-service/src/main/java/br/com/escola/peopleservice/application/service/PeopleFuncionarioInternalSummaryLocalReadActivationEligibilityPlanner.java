package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlan;

@Service
public class PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlanner {

    public PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlan planejarElegibilidadeDeAtivacao() {
        return new PeopleFuncionarioInternalSummaryLocalReadActivationEligibilityPlan(
                "Fase 83",
                "funcionario_internal_summary_local_read_activation_eligibility",
                "internal_funcionario_local_read_guarded_without_external_route",
                "close_phase_83_and_only_consider_funcionario_internal_usage_when_guard_is_green",
                "funcionario_internal_summary_internal_usage_candidate",
                true,
                true,
                false,
                false,
                true,
                "funcionarioInternalSummaryLocalRead",
                "people_funcionario_read_model",
                "monolith_internal_rh",
                List.of(
                        "people.shadow.local-persistence.enabled=true",
                        "people.shadow.local-persistence.read-model-cutover-enabled=true",
                        "people.shadow.local-persistence.backfill-enabled=true",
                        "people.shadow.local-persistence.reconciliation-enabled=true",
                        "people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "localReadModelBackfill.status=completed",
                        "localReadModelBackfill.divergences=0",
                        "funcionario-internal-summary-source-has-no-duplicate-pessoa-id",
                        "funcionario-internal-summary-read-model-has-no-school-ownership-divergence"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                        "keep-funcionario-read-on-monolith-internal-rh-when-guard-is-not-green",
                        "rerun-funcionario-backfill-and-reconciliation-before-any-internal-activation"),
                List.of(
                        "new-internal-rest-route",
                        "query-service-external-connection",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "auth-cutover"));
    }
}
