package br.com.escola.historico.domain.exception;

public class HistoricoEscolarInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public HistoricoEscolarInvalidoException(String message) {
        super(message);
    }
}
