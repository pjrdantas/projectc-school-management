package br.com.escola.enrollment.domain.exception;

import java.util.UUID;

public class MatriculaPeriodoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaPeriodoNaoEncontradoException(UUID periodoLetivoId) {
        super("Período letivo não encontrado para o id " + periodoLetivoId);
    }
}
