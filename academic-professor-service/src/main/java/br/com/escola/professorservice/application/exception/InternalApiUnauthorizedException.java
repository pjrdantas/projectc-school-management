package br.com.escola.professorservice.application.exception;

public class InternalApiUnauthorizedException extends RuntimeException {

    public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
