package br.com.escola.bff.interfaces.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaDisciplinaResponse(
        UUID id,
        UUID turmaId,
        UUID disciplinaId,
        String disciplinaNome,
        Integer cargaHoraria,
        LocalDateTime createdAt
) {}
