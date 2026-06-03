package br.com.escola.matricula.application.dto;

import java.util.UUID;

public record MatriculaInput(
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId,
        String tipoMatricula,
        String observacao) {
}
