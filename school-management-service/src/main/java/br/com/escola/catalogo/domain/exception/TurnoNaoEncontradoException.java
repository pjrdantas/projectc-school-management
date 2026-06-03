package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class TurnoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public TurnoNaoEncontradoException(UUID id) {
        super("Turno não encontrado para o id " + id);
    }
}
