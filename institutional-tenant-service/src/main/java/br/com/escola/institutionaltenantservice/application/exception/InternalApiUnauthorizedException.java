package br.com.escola.institutionaltenantservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
