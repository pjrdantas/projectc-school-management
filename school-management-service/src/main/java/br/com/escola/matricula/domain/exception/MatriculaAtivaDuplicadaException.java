package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaAtivaDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public MatriculaAtivaDuplicadaException(UUID alunoId, UUID periodoLetivoId) {
        super("Aluno " + alunoId + " já possui matrícula ATIVA no período letivo " + periodoLetivoId);
    }
}
