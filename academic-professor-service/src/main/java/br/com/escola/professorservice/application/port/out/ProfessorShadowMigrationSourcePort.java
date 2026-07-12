package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.migration.ProfessorShadowMigrationSnapshot;

public interface ProfessorShadowMigrationSourcePort {

    ProfessorShadowMigrationSnapshot carregarSnapshot();
}
