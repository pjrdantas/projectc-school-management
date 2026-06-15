package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record DisciplinaResumo(
        UUID id,
        String nome,
        Integer cargaHoraria,
        boolean ativo
) {
}
