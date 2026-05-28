package br.com.escola.transfermanagement.domain.exception;

public class TransferenciaAlunoInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public TransferenciaAlunoInvalidaException(String message) {
        super(message);
    }
}
