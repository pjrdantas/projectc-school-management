package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class AulaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaNaoEncontradaException(UUID id) {
        super("Aula não encontrada para o id " + id);
    }

    public AulaNaoEncontradaException(String message) {
        super(message);
    }
}
