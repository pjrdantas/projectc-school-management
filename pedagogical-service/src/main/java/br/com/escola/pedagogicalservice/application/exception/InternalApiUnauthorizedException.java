package br.com.escola.pedagogicalservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
