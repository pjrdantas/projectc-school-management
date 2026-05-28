package br.com.escola.enrollment.domain.exception;

import java.util.UUID;

public class MatriculaTurmaSemVagaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaTurmaSemVagaException(UUID turmaId, Integer capacidade) {
        super("Turma " + turmaId + " não possui vagas disponíveis. Limite de matrículas: " + capacidade);
    }
}
