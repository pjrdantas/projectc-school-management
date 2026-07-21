package br.com.escola.enrollmentdocumentservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidRequestContextException(String message) {
        super(message);
    }
}
