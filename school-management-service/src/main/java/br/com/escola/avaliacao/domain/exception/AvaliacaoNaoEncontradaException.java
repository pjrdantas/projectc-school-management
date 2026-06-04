package br.com.escola.avaliacao.domain.exception;

import java.util.UUID;

public class AvaliacaoNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNaoEncontradaException(UUID id) {
        super("Avaliação não encontrada para o id " + id);
    }
}
