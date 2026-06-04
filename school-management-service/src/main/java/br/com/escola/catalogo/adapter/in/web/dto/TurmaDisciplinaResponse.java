package br.com.escola.catalogo.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaDisciplinaResponse(
        UUID id,
        UUID turmaId,
        UUID disciplinaId,
        String disciplinaNome,
        Integer cargaHoraria,
        LocalDateTime createdAt) {
}
