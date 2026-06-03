package br.com.escola.transferencia.domain.exception;

import java.util.UUID;

public class EscolaOrigemNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public EscolaOrigemNaoEncontradaException(UUID id) {
        super("Escola de origem não encontrada: " + id);
    }
}
