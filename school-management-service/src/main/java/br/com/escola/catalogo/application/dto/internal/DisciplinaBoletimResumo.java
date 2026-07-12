package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record DisciplinaBoletimResumo(
        UUID disciplinaId,
        String nome,
        Integer cargaHoraria) {
}
