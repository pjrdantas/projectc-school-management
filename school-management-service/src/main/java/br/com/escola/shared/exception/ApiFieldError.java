package br.com.escola.shared.exception;

public record ApiFieldError(
        String field,
        String message
) {
}
