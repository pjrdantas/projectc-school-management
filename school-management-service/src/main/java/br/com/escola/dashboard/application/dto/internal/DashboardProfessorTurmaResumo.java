package br.com.escola.dashboard.application.dto.internal;

import java.util.UUID;

public record DashboardProfessorTurmaResumo(
        UUID professorTurmaDisciplinaId,
        UUID turmaId,
        String turmaNome,
        UUID disciplinaId,
        String disciplinaNome) {
}
