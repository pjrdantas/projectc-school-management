package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaAlunoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaAlunoNaoEncontradoException(UUID alunoId) {
        super("Aluno não encontrado para o id " + alunoId);
    }
}
