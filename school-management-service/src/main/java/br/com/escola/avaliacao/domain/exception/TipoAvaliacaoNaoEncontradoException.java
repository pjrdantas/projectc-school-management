package br.com.escola.avaliacao.domain.exception;

public class TipoAvaliacaoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TipoAvaliacaoNaoEncontradoException() {
        super("Tipo de avaliação não encontrado.");
    }
}
