package br.com.escola.professor.domain.exception;

public class SituacaoFrequenciaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SituacaoFrequenciaNaoEncontradaException() {
        super("Situação de frequência não encontrada.");
    }
}
