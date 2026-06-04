package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class AulaMatriculaTurmaInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaMatriculaTurmaInconsistenteException(UUID aulaId, UUID matriculaId) {
        super("Matrícula " + matriculaId + " não pertence à turma da aula " + aulaId);
    }
}
