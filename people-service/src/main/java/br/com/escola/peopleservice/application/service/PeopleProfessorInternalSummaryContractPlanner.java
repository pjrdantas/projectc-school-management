package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleProfessorInternalSummaryContractPlan;

@Service
public class PeopleProfessorInternalSummaryContractPlanner {

    public PeopleProfessorInternalSummaryContractPlan planejarContratoInternoResumoProfessor() {
        return new PeopleProfessorInternalSummaryContractPlan(
                "Fase 95",
                "professor_internal_summary_contract",
                "internal_contract_prepared_no_adapter_no_route",
                "close_phase_95_and_plan_professor_internal_summary_adapter_preparation",
                "professor_internal_summary_adapter_preparation",
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                "people_professor_read_model_candidate",
                "monolith_internal_professor",
                true,
                List.of(
                        "id_professor",
                        "id_pessoa",
                        "id_funcionario",
                        "id_escola",
                        "nome_completo",
                        "ativo"),
                List.of(
                        "professor-internal-read-diagnostic",
                        "future-professor-read-connection-without-academic-allocation"),
                List.of(
                        "sem entidade JPA de professor no contrato interno",
                        "sem alocacao professor turma disciplina dentro do people-service",
                        "sem rota externa",
                        "sem BFF e sem frontend",
                        "sem adapter JDBC nesta subfase",
                        "sem mover write de professor, funcionario ou autenticacao"),
                List.of(
                        "manter professor no monolito como unica fonte funcional",
                        "nao conectar adapter local enquanto a proxima subfase nao decidir a estrategia de leitura",
                        "nao redirecionar rotas externas de professor para o people-service"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "professor-write-cutover",
                        "professor-turma-disciplina-cutover",
                        "auth-cutover"));
    }
}
