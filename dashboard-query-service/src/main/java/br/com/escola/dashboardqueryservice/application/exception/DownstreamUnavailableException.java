package br.com.escola.dashboardqueryservice.application.exception;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
