package br.com.escola.professorservice.application.exception;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownstreamUnavailableException(String message) {
        super(message);
    }
}
