package br.com.escola.pedagogicalservice.application.exception;

public class ConflitoNegocioException extends RuntimeException {
    public ConflitoNegocioException(String message) {
        super(message);
    }
}
