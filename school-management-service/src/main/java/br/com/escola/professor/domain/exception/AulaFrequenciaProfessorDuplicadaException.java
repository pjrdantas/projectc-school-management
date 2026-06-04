package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class AulaFrequenciaProfessorDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaFrequenciaProfessorDuplicadaException(UUID aulaId, UUID professorId) {
        super("Frequência do professor já registrada para a aula " + aulaId + " e professor " + professorId);
    }
}
