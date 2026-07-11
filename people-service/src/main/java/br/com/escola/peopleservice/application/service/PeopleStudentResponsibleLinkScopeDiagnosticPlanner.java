package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleStudentResponsibleLinkScopeDiagnosticPlan;

@Service
public class PeopleStudentResponsibleLinkScopeDiagnosticPlanner {

    public PeopleStudentResponsibleLinkScopeDiagnosticPlan planejarDiagnosticoEscopoVinculosAlunoResponsavel() {
        return new PeopleStudentResponsibleLinkScopeDiagnosticPlan(
                "Fase 96",
                "student_responsible_link_contract_diagnostic",
                "student_responsible_person_link_contracts_exist_without_explicit_family_closure",
                "prepare_minimal_student_responsible_link_contract_closure_without_new_route_or_persistence",
                "student_responsible_link_contract_closure",
                true,
                true,
                false,
                false,
                true,
                List.of(
                        "resolver alunoId para pessoaId por escola",
                        "resolver responsavelId para pessoaId por escola",
                        "preservar lookup de consulta cadastral entre aluno e responsavel"),
                List.of(
                        "nenhum write de aluno, responsavel ou aluno_responsavel deve sair do monolito nesta macrofase"),
                List.of(
                        "aluno e responsavel ainda dependem do cadastro base de pessoa no monolito",
                        "aluno_responsavel continua compondo consulta cadastral e consumidores documentais",
                        "parentesco e status_aluno permanecem sem contrato proprio no people-service"),
                List.of(
                        "lookup por escola deve continuar obrigatorio para evitar cruzamento entre tenants",
                        "a familia mistura pessoa base e vinculo aluno_responsavel, mas nao deve absorver writes agora",
                        "qualquer uso futuro de parentesco exige separar catalogo de vinculo do lookup simples"),
                List.of(
                        "primeiro fechamento deve reutilizar services e adapters locais ja existentes",
                        "sem migration nova nesta subfase diagnostica",
                        "se houver evolucao futura, separar lookup de vinculo e catalogos de parentesco/status_aluno"),
                List.of(
                        "manter todos os fluxos oficiais de aluno e responsavel no monolito",
                        "nao redirecionar consumidores reais fora do people-service nesta fase",
                        "nao ampliar consultarCadastro nem rotas externas para forcar uso da familia"),
                List.of(
                        "primeira implementacao deve ser apenas formalizacao da familia ja existente",
                        "sem rota externa, sem BFF, sem frontend e sem persistencia propria adicional",
                        "sem misturar parentesco, status_aluno ou writes de aluno/responsavel nesta macrofase"),
                List.of(
                        "new-external-route",
                        "bff-route-change",
                        "frontend-change",
                        "student-write-cutover",
                        "responsible-write-cutover",
                        "consultarCadastro-payload-change"));
    }
}
