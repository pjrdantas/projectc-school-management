package br.com.escola.aluno.domain.exception;

public class AlunoJaCadastradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlunoJaCadastradoException() {
        super("Já existe aluno cadastrado com este CPF");
    }
}
