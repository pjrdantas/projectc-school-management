package br.com.escola.professorservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
