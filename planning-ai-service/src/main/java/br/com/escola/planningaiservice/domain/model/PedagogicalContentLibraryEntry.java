package br.com.escola.planningaiservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record PedagogicalContentLibraryEntry(
        UUID id,
        UUID escolaId,
        UUID conteudoOrigemId,
        UUID professorId,
        UUID disciplinaId,
        String tipoConteudo,
        String titulo,
        String tema,
        String conteudo,
        String origem,
        boolean reutilizavel,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
