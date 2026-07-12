package br.com.escola.enrollmentdocumentservice.application.exception;

public class InvalidRequestContextException extends RuntimeException {

    public InvalidRequestContextException(String message) {
        super(message);
    }
}
