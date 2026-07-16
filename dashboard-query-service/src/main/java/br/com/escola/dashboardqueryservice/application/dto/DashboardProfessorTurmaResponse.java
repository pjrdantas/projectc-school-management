package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record DashboardProfessorTurmaResponse(
        UUID professorTurmaDisciplinaId,
        UUID turmaId,
        String turmaNome,
        UUID disciplinaId,
        String disciplinaNome) {
}
