package br.com.escola.seguranca.domain.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
