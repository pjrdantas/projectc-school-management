package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardProfessorTurmaResponse(
        UUID professorTurmaDisciplinaId,
        UUID turmaId,
        String turmaNome,
        UUID disciplinaId,
        String disciplinaNome) {
}
