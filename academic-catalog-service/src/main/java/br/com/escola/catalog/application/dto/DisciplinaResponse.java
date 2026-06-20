package br.com.escola.catalog.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisciplinaResponse(
        UUID id,
        String nome,
        Integer cargaHoraria,
        boolean ativo,
        UUID escolaId,
        LocalDateTime createdAt) {
}
