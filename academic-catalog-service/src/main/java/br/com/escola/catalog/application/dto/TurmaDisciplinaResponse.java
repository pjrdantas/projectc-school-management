package br.com.escola.catalog.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaDisciplinaResponse(
        UUID id,
        UUID turmaId,
        UUID disciplinaId,
        String disciplinaNome,
        Integer cargaHoraria,
        UUID escolaId,
        LocalDateTime createdAt) {
}
