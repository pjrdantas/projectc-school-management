package br.com.escola.dashboardqueryservice.application.exception;

public class PainelQueryServiceConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PainelQueryServiceConflictException(String message) {
        super(message);
    }
}
