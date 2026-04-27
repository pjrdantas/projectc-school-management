package br.com.escola.studentmanagement.domain.exception;

public class AlunoJaCadastradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlunoJaCadastradoException() {
        super("Já existe aluno cadastrado com este CPF");
    }
}
