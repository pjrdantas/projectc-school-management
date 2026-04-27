package br.com.escola.enrollment.domain.exception;

public class MatriculaAlunoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaAlunoNaoEncontradoException(Long alunoId) {
        super("Aluno não encontrado para o id " + alunoId);
    }
}
