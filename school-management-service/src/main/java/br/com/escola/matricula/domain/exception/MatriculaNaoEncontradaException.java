package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public MatriculaNaoEncontradaException(UUID id) {
        super("Matrícula não encontrada para o id " + id);
    }
}
