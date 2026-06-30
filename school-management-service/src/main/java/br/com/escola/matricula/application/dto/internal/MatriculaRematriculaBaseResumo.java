package br.com.escola.matricula.application.dto.internal;

import java.util.UUID;

public record MatriculaRematriculaBaseResumo(
        UUID matriculaBaseId,
        UUID alunoId,
        String statusBase) {
}
