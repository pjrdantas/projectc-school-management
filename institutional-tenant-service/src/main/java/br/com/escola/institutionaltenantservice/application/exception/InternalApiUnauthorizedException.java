package br.com.escola.institutionaltenantservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
