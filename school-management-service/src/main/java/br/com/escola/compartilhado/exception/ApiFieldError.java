package br.com.escola.compartilhado.exception;

public record ApiFieldError(
        String field,
        String message
) {
}
