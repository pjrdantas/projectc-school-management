package br.com.escola.avaliacao.domain.exception;

public class TipoAvaliacaoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TipoAvaliacaoNaoEncontradoException(String codigo) {
        super("Tipo de avaliação não encontrado para o código " + codigo);
    }
}
