package br.com.escola.responsavelmanagement.domain.exception;

public class ResponsavelJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponsavelJaCadastradoException() {
        super("Já existe responsável cadastrado com este CPF");
    }
}
