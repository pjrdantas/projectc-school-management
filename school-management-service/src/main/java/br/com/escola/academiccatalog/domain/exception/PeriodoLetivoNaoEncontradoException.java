package br.com.escola.academiccatalog.domain.exception;

public class PeriodoLetivoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PeriodoLetivoNaoEncontradoException(Long id) {
        super("Período letivo não encontrado para o id " + id);
    }
}
