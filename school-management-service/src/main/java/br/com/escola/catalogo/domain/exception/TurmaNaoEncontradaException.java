package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class TurmaNaoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TurmaNaoEncontradaException(UUID id) {
        super("Turma não encontrada para o id " + id);
    }
}
