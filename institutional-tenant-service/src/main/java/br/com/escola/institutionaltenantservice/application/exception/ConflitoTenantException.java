package br.com.escola.institutionaltenantservice.application.exception;

public class ConflitoTenantException extends RuntimeException {

    public ConflitoTenantException(String message) {
        super(message);
    }
}
