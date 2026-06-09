package br.com.escola.professor.domain.exception;

public class ProfessorTurmaDisciplinaDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorTurmaDisciplinaDuplicadaException() {
        super("Professor já está vinculado a esta turma e disciplina.");
    }
}
