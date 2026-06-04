package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaEtapaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaEtapaNaoEncontradaException(UUID id) {
        super("Etapa de matrícula não encontrada para o id " + id);
    }
}
