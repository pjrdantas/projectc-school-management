package br.com.escola.professor.domain.exception;

public class ProfessorFuncionarioInativoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorFuncionarioInativoException() {
        super("Funcionário inativo não pode ser cadastrado como professor.");
    }
}
