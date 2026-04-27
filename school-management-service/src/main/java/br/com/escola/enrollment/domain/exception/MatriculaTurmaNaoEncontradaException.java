package br.com.escola.enrollment.domain.exception;

public class MatriculaTurmaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaTurmaNaoEncontradaException(Long turmaId) {
        super("Turma não encontrada para o id " + turmaId);
    }
}
