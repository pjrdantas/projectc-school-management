package br.com.escola.bff.application.exception;

public class DownstreamRejectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int status;

    public DownstreamRejectedException(int status) {
        super("Servico interno rejeitou a requisicao");
        this.status = status;
    }

    public int status() {
        return status;
    }
}

