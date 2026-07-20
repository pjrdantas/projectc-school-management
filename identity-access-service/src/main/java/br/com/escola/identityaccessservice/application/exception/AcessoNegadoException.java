package br.com.escola.identityaccessservice.application.exception;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String message) {
        super(message);
    }
}
