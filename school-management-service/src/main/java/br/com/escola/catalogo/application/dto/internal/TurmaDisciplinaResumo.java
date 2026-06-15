package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record TurmaDisciplinaResumo(
        UUID id,
        UUID turmaId,
        UUID disciplinaId,
        String disciplinaNome,
        Integer cargaHoraria
) {
}
