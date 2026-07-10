package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryAdapterPreparationPlan;

@Service
public class PeopleFuncionarioInternalSummaryAdapterPreparationPlanner {

    public PeopleFuncionarioInternalSummaryAdapterPreparationPlan planejarPreparacaoDoAdapterLocal() {
        return new PeopleFuncionarioInternalSummaryAdapterPreparationPlan(
                "Fase 81",
                "funcionario_internal_summary_adapter_preparation",
                "jdbc_local_adapter_prepared_internal_fallback_only",
                "close_phase_81_and_plan_funcionario_internal_summary_backfill_reconciliation_preparation",
                "funcionario_internal_summary_backfill_reconciliation_preparation",
                true,
                true,
                true,
                false,
                false,
                "people_funcionario_read_model",
                "monolith_internal_rh",
                "funcionarioInternalSummaryLocalRead",
                "V6__create_people_funcionario_internal_summary_read_model.sql",
                Map.of(
                        "port", "PeopleFuncionarioInternalSummaryPort",
                        "response", "PessoaFuncionarioInternalSummaryResponse",
                        "adapter", "JdbcPeopleFuncionarioInternalSummaryAdapter",
                        "internalService", "PeopleFuncionarioInternalSummaryService",
                        "adapterCreated", true,
                        "internalServiceConnected", true,
                        "migrationCreated", true,
                        "routeCreated", false,
                        "queryServiceConnected", false),
                List.of(
                        "schema-migration-must-remain-opt-in",
                        "backfill-and-reconciliation-must-be-green-before-any-activation",
                        "professor-eligibility-must-stay-outside-people-service",
                        "auth-context-must-not-consume-local-funcionario-before-explicit-phase"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.migration-enabled"),
                List.of(
                        "create-funcionario-route-in-people-service",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "professor-write-change",
                        "auth-cutover"));
    }
}
