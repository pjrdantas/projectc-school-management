package br.com.escola.professor.domain.exception;

public class ProfessorJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorJaCadastradoException() {
        super("Professor já cadastrado para este funcionário.");
    }
}
