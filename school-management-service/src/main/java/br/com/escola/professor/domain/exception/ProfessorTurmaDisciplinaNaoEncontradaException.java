package br.com.escola.professor.domain.exception;

public class ProfessorTurmaDisciplinaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorTurmaDisciplinaNaoEncontradaException() {
        super("Turma/disciplina não encontrada.");
    }
}
