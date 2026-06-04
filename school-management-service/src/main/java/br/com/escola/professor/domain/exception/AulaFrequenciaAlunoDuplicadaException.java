package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class AulaFrequenciaAlunoDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaFrequenciaAlunoDuplicadaException(UUID aulaId, UUID matriculaId) {
        super("Frequência do aluno já registrada para a aula " + aulaId + " e matrícula " + matriculaId);
    }
}
