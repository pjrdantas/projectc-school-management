package br.com.escola.catalog.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
