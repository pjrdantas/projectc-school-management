package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlan;

@Service
public class PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlanner {

    public PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlan planejarBackfillReconciliacao() {
        return new PeopleFuncionarioInternalSummaryBackfillReconciliationPreparationPlan(
                "Fase 82",
                "funcionario_internal_summary_backfill_reconciliation_preparation",
                "funcionario_internal_summary_backfill_reconciliation_prepared_no_read_cutover",
                "close_phase_82_and_keep_funcionario_internal_summary_local_read_blocked_until_green",
                "funcionario_internal_summary_local_read_activation_eligibility",
                true,
                true,
                true,
                false,
                "monolith_jdbc",
                "people_funcionario_read_model",
                "people_funcionario_read_model.id_funcionario",
                List.of(
                        "funcionario",
                        "pessoa",
                        "cargo",
                        "escola"),
                List.of(
                        "duplicate-funcionario-id-in-source-blocks-green-reconciliation",
                        "same-pessoa-linked-to-multiple-funcionarios-blocks-green-reconciliation",
                        "cargo-descricao-drift-blocks-green-reconciliation",
                        "ownership-by-pessoa.id_escola-must-match-target"),
                List.of(
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled"),
                List.of(
                        "external-route-change",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "professor-write-change",
                        "auth-cutover"));
    }
}
