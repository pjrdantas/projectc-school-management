package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeopleProfessorScopeDiagnosticPlannerTest {

    @Test
    void devePlanejarDiagnosticoDoEscopoProfessor() {
        PeopleProfessorScopeDiagnosticPlanner planner = new PeopleProfessorScopeDiagnosticPlanner();

        var plan = planner.planejarDiagnosticoEscopoProfessor();

        assertThat(plan.phase()).isEqualTo("Fase 95");
        assertThat(plan.slice()).isEqualTo("professor_contract_diagnostic");
        assertThat(plan.status())
                .isEqualTo("professor_summary_contract_preferred_with_funcionario_and_person_authority_preserved_on_monolith");
        assertThat(plan.recommendedNextStep())
                .isEqualTo("prepare_minimal_professor_internal_summary_contract_without_route_or_persistence_cutover");
        assertThat(plan.minimalNextSlice()).isEqualTo("professor_internal_summary_contract");
        assertThat(plan.diagnosticReadyNow()).isTrue();
        assertThat(plan.internalContractSeparationAllowedNow()).isTrue();
        assertThat(plan.localPersistenceAllowedNow()).isFalse();
        assertThat(plan.externalRouteChangeAllowedNow()).isFalse();
        assertThat(plan.fallbackToCurrentMonolithRequired()).isTrue();
        assertThat(plan.minimalReadCandidates()).contains(
                "buscar professor por id dentro da escola",
                "listar professores por escola para consumo interno futuro");
        assertThat(plan.monolithDependencies()).contains(
                "professor depende de funcionario como elegibilidade e de pessoa como identidade base",
                "professor atual conversa com catalogo academico para turma disciplina fora do people-service");
        assertThat(plan.firstImplementationGuardrails()).contains(
                "primeira implementacao deve cobrir apenas resumo base de professor por escola",
                "sem BFF, sem frontend, sem cutover e sem tocar people-service alem do novo contrato");
    }
}
