package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class DisciplinaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DisciplinaNaoEncontradaException(UUID id) {
        super("Disciplina não encontrada para o id " + id);
    }
}
