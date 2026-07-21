package br.com.escola.planningaiservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidRequestContextException(String message) {
        super(message);
    }
}
