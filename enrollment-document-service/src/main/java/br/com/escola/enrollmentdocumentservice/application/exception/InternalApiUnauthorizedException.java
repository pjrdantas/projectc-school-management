package br.com.escola.enrollmentdocumentservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
