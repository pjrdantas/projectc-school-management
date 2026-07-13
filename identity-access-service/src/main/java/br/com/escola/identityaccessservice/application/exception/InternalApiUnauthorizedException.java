package br.com.escola.identityaccessservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
