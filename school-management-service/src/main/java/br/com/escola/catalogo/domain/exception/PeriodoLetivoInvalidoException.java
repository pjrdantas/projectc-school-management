package br.com.escola.catalogo.domain.exception;

public class PeriodoLetivoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PeriodoLetivoInvalidoException() {
        super("dataFim deve ser maior ou igual a dataInicio");
    }
}
