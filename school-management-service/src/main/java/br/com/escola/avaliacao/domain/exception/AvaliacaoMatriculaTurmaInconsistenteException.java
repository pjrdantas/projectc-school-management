package br.com.escola.avaliacao.domain.exception;

import java.util.UUID;

public class AvaliacaoMatriculaTurmaInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoMatriculaTurmaInconsistenteException(UUID avaliacaoId, UUID matriculaId) {
        super("Matrícula " + matriculaId + " não pertence à turma da avaliação " + avaliacaoId);
    }
}
