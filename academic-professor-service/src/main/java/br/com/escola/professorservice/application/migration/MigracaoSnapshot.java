package br.com.escola.professorservice.application.migration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MigracaoSnapshot(
        List<CadastroRow> professores) {

    public MigracaoSnapshot {
        professores = List.copyOf(professores);
    }

    public record CadastroRow(
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

