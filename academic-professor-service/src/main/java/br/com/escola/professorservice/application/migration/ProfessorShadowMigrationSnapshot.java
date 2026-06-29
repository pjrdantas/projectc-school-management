package br.com.escola.professorservice.application.migration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProfessorShadowMigrationSnapshot(
        List<ProfessorRow> professores) {

    public ProfessorShadowMigrationSnapshot {
        professores = List.copyOf(professores);
    }

    public record ProfessorRow(
            UUID id,
            UUID pessoaId,
            UUID escolaId,
            String escolaNome,
            String nomeCompleto,
            String registroProfissional,
            String formacao,
            boolean ativo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UUID usuarioId) {
    }
}
