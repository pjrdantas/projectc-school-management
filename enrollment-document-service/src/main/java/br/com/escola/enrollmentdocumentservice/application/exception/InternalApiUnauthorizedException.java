package br.com.escola.enrollmentdocumentservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
