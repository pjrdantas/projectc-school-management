package br.com.escola.enrollmentdocumentservice.application.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String message) {
        super(message);
    }
}
