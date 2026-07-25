package br.com.escola.catalog.application.exception;

public class ConflitoNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ConflitoNegocioException(String message) {
        super(message);
    }
}
