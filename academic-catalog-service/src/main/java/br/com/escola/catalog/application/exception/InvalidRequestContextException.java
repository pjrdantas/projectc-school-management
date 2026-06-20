package br.com.escola.catalog.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
