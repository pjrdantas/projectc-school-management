package br.com.escola.institutionaltenantservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
