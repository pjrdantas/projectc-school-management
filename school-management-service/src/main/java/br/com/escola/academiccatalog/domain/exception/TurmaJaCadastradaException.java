package br.com.escola.academiccatalog.domain.exception;

public class TurmaJaCadastradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TurmaJaCadastradaException(String codigo, Long periodoLetivoId) {
        super("Já existe turma com código " + codigo + " para o período letivo " + periodoLetivoId);
    }
}
