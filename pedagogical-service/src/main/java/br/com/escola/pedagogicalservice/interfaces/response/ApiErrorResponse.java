package br.com.escola.pedagogicalservice.interfaces.response;

public record ApiErrorResponse(
        String error,
        String message) {
}
