package br.com.escola.professor.domain.exception;

public class AulaFrequenciaProfessorDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaFrequenciaProfessorDuplicadaException() {
        super("Frequência do professor já registrada para esta aula.");
    }
}
