package br.com.escola.accesscontrol.domain.exception;

public class TokenInvalidoOuExpiradoException extends RuntimeException {
    public TokenInvalidoOuExpiradoException(String message) {
        super(message);
    }
}
