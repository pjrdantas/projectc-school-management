package br.com.escola.professor.domain.exception;

public class AulaFrequenciaAlunoDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaFrequenciaAlunoDuplicadaException() {
        super("Frequência do aluno já registrada para esta aula.");
    }
}
