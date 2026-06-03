package br.com.escola.seguranca.domain.exception;

public class TokenInvalidoOuExpiradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenInvalidoOuExpiradoException(String message) {
        super(message);
    }
}
