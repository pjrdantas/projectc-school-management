package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryContractPlan;

@Service
public class PeopleFuncionarioInternalSummaryContractPlanner {

    public PeopleFuncionarioInternalSummaryContractPlan planejarContratoInternoResumoFuncionario() {
        return new PeopleFuncionarioInternalSummaryContractPlan(
                "Fase 80",
                "funcionario_internal_summary_contract",
                "internal_contract_prepared_no_adapter_no_route",
                "close_phase_80_and_plan_funcionario_internal_summary_adapter_preparation",
                "funcionario_internal_summary_adapter_preparation",
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                "people_funcionario_read_model_candidate",
                "monolith_internal_rh",
                true,
                List.of(
                        "id_funcionario",
                        "id_pessoa",
                        "id_escola",
                        "nome_completo",
                        "cargo_descricao",
                        "ativo"),
                List.of(
                        "professor-eligibility-read-evaluation",
                        "auth-professor-context-diagnostic"),
                List.of(
                        "sem entidade JPA de RH no contrato interno",
                        "sem regra de elegibilidade de professor dentro do people-service",
                        "sem rota externa",
                        "sem BFF e sem frontend",
                        "sem adapter JDBC nesta subfase",
                        "sem mover write de funcionario, usuario ou autenticacao"),
                List.of(
                        "manter RH interno do monolito como unica fonte funcional",
                        "nao conectar adapter local enquanto nao houver decisao explicita da proxima subfase",
                        "nao redirecionar professor ou autenticacao para o people-service"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "auth-cutover",
                        "professor-write-change"));
    }
}
