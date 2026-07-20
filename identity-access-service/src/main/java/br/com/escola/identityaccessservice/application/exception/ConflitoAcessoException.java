package br.com.escola.identityaccessservice.application.exception;

public class ConflitoAcessoException extends RuntimeException {

    public ConflitoAcessoException(String message) {
        super(message);
    }
}
