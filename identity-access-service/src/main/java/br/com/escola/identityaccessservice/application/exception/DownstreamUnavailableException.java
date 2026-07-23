package br.com.escola.identityaccessservice.application.exception;

public class DownstreamUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
