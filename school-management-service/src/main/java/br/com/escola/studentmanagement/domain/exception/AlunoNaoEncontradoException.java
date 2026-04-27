package br.com.escola.studentmanagement.domain.exception;

public class AlunoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlunoNaoEncontradoException(Long id) {
        super("Aluno não encontrado para o id " + id);
    }
}
