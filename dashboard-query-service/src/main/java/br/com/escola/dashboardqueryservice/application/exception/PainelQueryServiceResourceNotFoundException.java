package br.com.escola.dashboardqueryservice.application.exception;

public class PainelQueryServiceResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public PainelQueryServiceResourceNotFoundException(String message) {
        super(message);
    }
}

