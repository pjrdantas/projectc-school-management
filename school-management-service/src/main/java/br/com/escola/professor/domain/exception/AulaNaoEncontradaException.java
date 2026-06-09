package br.com.escola.professor.domain.exception;

public class AulaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaNaoEncontradaException() {
        super("Aula não encontrada.");
    }

    public AulaNaoEncontradaException(String message) {
        super(message);
    }
}
