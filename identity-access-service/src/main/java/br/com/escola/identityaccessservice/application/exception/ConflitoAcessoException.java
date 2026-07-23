package br.com.escola.identityaccessservice.application.exception;

public class ConflitoAcessoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ConflitoAcessoException(String message) {
        super(message);
    }
}
