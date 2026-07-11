package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleProfessorScopeDiagnosticPlan;

@Service
public class PeopleProfessorScopeDiagnosticPlanner {

    public PeopleProfessorScopeDiagnosticPlan planejarDiagnosticoEscopoProfessor() {
        return new PeopleProfessorScopeDiagnosticPlan(
                "Fase 95",
                "professor_contract_diagnostic",
                "professor_summary_contract_preferred_with_funcionario_and_person_authority_preserved_on_monolith",
                "prepare_minimal_professor_internal_summary_contract_without_route_or_persistence_cutover",
                "professor_internal_summary_contract",
                true,
                true,
                false,
                false,
                true,
                List.of(
                        "buscar professor por id dentro da escola",
                        "listar professores por escola para consumo interno futuro",
                        "listar professores por turma somente depois de contrato interno minimo estabilizado"),
                List.of(
                        "nenhum write de professor deve sair do monolito nesta macrofase",
                        "alocacao professor turma disciplina permanece fora do people-service nesta etapa"),
                List.of(
                        "professor depende de funcionario como elegibilidade e de pessoa como identidade base",
                        "professor atual conversa com catalogo academico para turma disciplina fora do people-service",
                        "vinculo com usuario e autenticacao permanece fora do people-service",
                        "consultas atuais de professor no backend legado misturam pessoa, funcionario e contexto escolar"),
                List.of(
                        "um mesmo professor depende de funcionario ativo e pessoa valida na mesma escola",
                        "o people-service nao deve absorver alocacao academica nem login como efeito colateral",
                        "resumo minimo precisa distinguir cadastro base de professor das relacoes academicas"),
                List.of(
                        "primeiro contrato deve ser somente read-only interno de resumo de professor",
                        "sem migration de professor nesta subfase diagnostica",
                        "qualquer persistencia futura deve separar professor de professor_turma_disciplina"),
                List.of(
                        "manter toda leitura oficial de professor no monolito",
                        "nao conectar people-service a rotas externas de professor nesta fase",
                        "nao iniciar local persistence de professor antes do contrato interno minimo"),
                List.of(
                        "primeira implementacao deve cobrir apenas resumo base de professor por escola",
                        "sem JPA compartilhada, sem entidade academica e sem dependencias de autenticacao",
                        "sem BFF, sem frontend, sem cutover e sem tocar people-service alem do novo contrato"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "professor-write-cutover",
                        "professor-turma-disciplina-migration",
                        "auth-or-usuario-change"));
    }
}
