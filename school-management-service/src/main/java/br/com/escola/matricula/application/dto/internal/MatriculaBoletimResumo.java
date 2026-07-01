package br.com.escola.matricula.application.dto.internal;

import java.util.UUID;

public record MatriculaBoletimResumo(
        UUID matriculaId,
        UUID alunoId,
        String alunoNome,
        UUID turmaId,
        String turmaNome,
        UUID periodoLetivoId,
        String periodoLetivoNome,
        UUID escolaId,
        String escolaNome) {
}
