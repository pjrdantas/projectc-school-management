package br.com.escola.academiccatalog.domain.exception;

import java.util.UUID;

public class TurmaCapacidadeInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TurmaCapacidadeInvalidaException(UUID turmaId, Integer capacidade, long matriculasAtivas) {
        super("A capacidade " + capacidade + " da turma " + turmaId
                + " não pode ser menor que as matrículas ativas: " + matriculasAtivas);
    }
}
