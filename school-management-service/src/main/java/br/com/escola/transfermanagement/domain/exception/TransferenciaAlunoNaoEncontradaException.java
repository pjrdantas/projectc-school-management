package br.com.escola.transfermanagement.domain.exception;

import java.util.UUID;

public class TransferenciaAlunoNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public TransferenciaAlunoNaoEncontradaException(UUID id) {
        super("Transferência do aluno não encontrada: " + id);
    }
}
