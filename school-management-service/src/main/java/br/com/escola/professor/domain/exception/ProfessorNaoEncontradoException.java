package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class ProfessorNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorNaoEncontradoException(UUID id) {
        super("Professor não encontrado para o id " + id);
    }

    public ProfessorNaoEncontradoException(String message) {
        super(message);
    }
}
