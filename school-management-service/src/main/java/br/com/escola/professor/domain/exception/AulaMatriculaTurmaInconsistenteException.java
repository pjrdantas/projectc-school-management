package br.com.escola.professor.domain.exception;

public class AulaMatriculaTurmaInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaMatriculaTurmaInconsistenteException() {
        super("A matrícula não pertence à turma desta aula.");
    }
}
