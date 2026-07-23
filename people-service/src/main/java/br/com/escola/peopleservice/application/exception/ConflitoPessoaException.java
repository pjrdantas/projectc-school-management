package br.com.escola.peopleservice.application.exception;

public class ConflitoPessoaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflitoPessoaException(String message) {
        super(message);
    }
}
