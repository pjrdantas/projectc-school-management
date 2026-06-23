package br.com.escola.bff.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaDisciplinaLinkedResult(
        UUID id,
        UUID turmaId,
        UUID disciplinaId,
        String disciplinaNome,
        Integer cargaHoraria,
        LocalDateTime createdAt
) {}
