package br.com.escola.institutionaltenantservice.application.exception;

public class ConflitoTenantException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ConflitoTenantException(String message) {
        super(message);
    }
}
