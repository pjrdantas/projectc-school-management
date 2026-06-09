package br.com.escola.avaliacao.domain.exception;

public class AvaliacaoNotaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNotaInvalidaException() {
        super("Nota inválida para o valor máximo da avaliação.");
    }
}
