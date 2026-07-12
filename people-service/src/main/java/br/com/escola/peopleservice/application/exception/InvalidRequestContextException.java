package br.com.escola.peopleservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
