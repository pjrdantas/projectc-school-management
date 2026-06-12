package br.com.escola.catalogo.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisciplinaResponse(
        UUID id,
        String nome,
        Integer cargaHoraria,
        String status,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt) {
}
