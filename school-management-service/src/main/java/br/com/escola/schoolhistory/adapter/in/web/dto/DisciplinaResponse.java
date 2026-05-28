package br.com.escola.schoolhistory.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisciplinaResponse(
        UUID id,
        String nome,
        Integer cargaHoraria,
        String status,
        LocalDateTime createdAt) {
}
