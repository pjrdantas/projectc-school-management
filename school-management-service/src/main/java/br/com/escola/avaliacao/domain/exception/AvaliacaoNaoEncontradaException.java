package br.com.escola.avaliacao.domain.exception;

public class AvaliacaoNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNaoEncontradaException() {
        super("Avaliação não encontrada.");
    }
}
