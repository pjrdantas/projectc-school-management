package br.com.escola.studentmanagement.domain.exception;

import java.util.UUID;

public class AlunoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlunoNaoEncontradoException(UUID id) {
        super("Aluno não encontrado para o id " + id);
    }
}
