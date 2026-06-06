package br.com.escola.professor.domain.exception;

public class ProfessorNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorNaoEncontradoException() {
        super("Professor não encontrado.");
    }

    public ProfessorNaoEncontradoException(String message) {
        super(message);
    }
}
