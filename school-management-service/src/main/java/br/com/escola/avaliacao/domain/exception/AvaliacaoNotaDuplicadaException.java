package br.com.escola.avaliacao.domain.exception;

import java.util.UUID;

public class AvaliacaoNotaDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNotaDuplicadaException(UUID avaliacaoId, UUID matriculaId) {
        super("Nota já lançada para a avaliação " + avaliacaoId + " e matrícula " + matriculaId);
    }
}
