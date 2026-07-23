package br.com.escola.catalog.application.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String message) {
        super(message);
    }
}
