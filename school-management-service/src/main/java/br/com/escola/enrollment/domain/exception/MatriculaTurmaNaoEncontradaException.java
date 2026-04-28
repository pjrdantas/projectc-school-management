package br.com.escola.enrollment.domain.exception;

import java.util.UUID;

public class MatriculaTurmaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaTurmaNaoEncontradaException(UUID turmaId) {
        super("Turma não encontrada para o id " + turmaId);
    }
}
