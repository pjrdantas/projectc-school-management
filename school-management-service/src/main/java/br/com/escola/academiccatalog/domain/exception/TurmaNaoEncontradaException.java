package br.com.escola.academiccatalog.domain.exception;

public class TurmaNaoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TurmaNaoEncontradaException(Long id) {
        super("Turma não encontrada para o id " + id);
    }
}
