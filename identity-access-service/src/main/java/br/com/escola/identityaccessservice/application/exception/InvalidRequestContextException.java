package br.com.escola.identityaccessservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
