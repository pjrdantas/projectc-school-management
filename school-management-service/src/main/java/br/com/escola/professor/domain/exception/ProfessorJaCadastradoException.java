package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class ProfessorJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorJaCadastradoException(UUID pessoaId) {
        super("Professor já cadastrado para a pessoa " + pessoaId);
    }
}
