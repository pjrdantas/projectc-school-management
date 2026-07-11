package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleProfessorInternalSummaryAdapterPreparationPlan;

@Service
public class PeopleProfessorInternalSummaryAdapterPreparationPlanner {

    public PeopleProfessorInternalSummaryAdapterPreparationPlan planejarPreparacaoDoAdapterLocal() {
        return new PeopleProfessorInternalSummaryAdapterPreparationPlan(
                "Fase 95",
                "professor_internal_summary_adapter_preparation",
                "adapter_preparation_diagnosed_contract_ready_keep_local_read_inactive",
                "close_phase_95_and_formally_finish_professor_initial_block",
                "professor_initial_block_closure",
                true,
                false,
                true,
                false,
                false,
                "people_professor_read_model_candidate",
                "monolith_internal_professor",
                "professorInternalSummaryLocalRead",
                "not-created-in-this-phase",
                Map.of(
                        "port", "PeopleProfessorInternalSummaryPort",
                        "response", "PessoaProfessorInternalSummaryResponse",
                        "adapter", "JdbcPeopleProfessorInternalSummaryAdapter",
                        "internalService", "PeopleProfessorInternalSummaryService",
                        "adapterCreated", false,
                        "internalServiceConnected", true,
                        "migrationCreated", false,
                        "routeCreated", false,
                        "queryServiceConnected", false),
                List.of(
                        "no-professor-read-model-schema-created-in-this-phase",
                        "funcionario-and-pessoa-dependencies-must-stay-explicit-before-any-local-adapter",
                        "academic-allocation-must-remain-outside-people-service",
                        "no-activation-without-a-future-green-reconciliation-plan"),
                List.of(
                        "keep-people-professor-contract-unconnected-to-any-external-route",
                        "do-not-create-professor-migration-in-this-phase",
                        "keep-people-service-reading-professor-only-from-monolith-contracts-until-next-macrophase"),
                List.of(
                        "create-professor-route-in-people-service",
                        "bff-route-change",
                        "frontend-change",
                        "professor-write-cutover",
                        "professor-turma-disciplina-cutover",
                        "auth-cutover"));
    }
}
