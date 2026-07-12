package br.com.escola.professorservice.application.exception;

public class DownstreamUnavailableException extends RuntimeException {



	private static final long serialVersionUID = 1L;

	public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownstreamUnavailableException(String message) {
        super(message);
    }
}
