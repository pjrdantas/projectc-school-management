package br.com.escola.professorservice.application.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String message) {
        super(message);
    }
}
