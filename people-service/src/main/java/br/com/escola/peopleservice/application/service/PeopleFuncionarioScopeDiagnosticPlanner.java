package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeDiagnosticPlan;

@Service
public class PeopleFuncionarioScopeDiagnosticPlanner {

    public PeopleFuncionarioScopeDiagnosticPlan planejarDiagnosticoEscopoFuncionario() {
        return new PeopleFuncionarioScopeDiagnosticPlan(
                "Fase 79",
                "funcionario_contract_diagnostic",
                "employee_summary_contract_preferred_professor_and_auth_dependencies_preserved_on_monolith",
                "prepare_minimal_funcionario_internal_summary_contract_without_route_or_persistence_cutover",
                "funcionario_internal_summary_contract",
                true,
                true,
                false,
                false,
                true,
                List.of(
                        "buscar funcionario por id da escola para elegibilidade de professor",
                        "listar funcionarios elegiveis para professor sem expor entidade RH",
                        "mapear resumo minimo de funcionario ativo para contexto interno"),
                List.of(
                        "nenhum write de funcionario deve sair do monolito nesta macrofase",
                        "vinculos de cargo, pessoa e usuario permanecem no monolito"),
                List.of(
                        "FuncionarioProfessorInternalController GET /internal/funcionarios/{id}/professor",
                        "FuncionarioProfessorInternalController GET /internal/funcionarios/professor-elegiveis",
                        "FuncionarioProfessorService + FuncionarioJpaRepository + PessoaConsultaPort + ProfessorPessoaPort",
                        "IdentidadeTenantService resolverProfessorId dependente de professor/usuario dentro do monolito",
                        "cadastro e ativacao de funcionario continuam acoplados a pessoa/cargo/usuario"),
                List.of(
                        "elegibilidade de professor depende de funcionario ativo mais ausencia de professor para a mesma pessoa/escola",
                        "contexto multi-escola depende de pessoa.escola e nao apenas de funcionario",
                        "cargo e status ativo influenciam resposta funcional e nao podem ser reduzidos indevidamente"),
                List.of(
                        "se houver proxima subfase pratica, migrar primeiro um resumo interno read-only de funcionario por escola",
                        "preservar chave por id_funcionario com referencia obrigatoria a id_pessoa e escola",
                        "nao criar migration de escrita, usuario ou cargo nesta etapa"),
                List.of(
                        "manter leituras e escritas de funcionario no monolito",
                        "desligar qualquer contrato interno novo e manter professor/auth consumindo apenas o backend atual",
                        "nao abrir runtime fisico nem cutover de autenticacao ou professor"),
                List.of(
                        "primeira implementacao deve ser somente resumo interno read-only",
                        "sem entidade JPA compartilhada fora do dominio RH",
                        "sem rota externa nova, sem BFF e sem frontend",
                        "sem tocar autenticacao, cadastro de professor ou write de funcionario na mesma fase"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "funcionario-write-cutover",
                        "auth-cutover",
                        "professor-write-change"));
    }
}
