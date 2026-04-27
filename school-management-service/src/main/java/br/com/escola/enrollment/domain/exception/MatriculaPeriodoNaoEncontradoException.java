package br.com.escola.enrollment.domain.exception;

public class MatriculaPeriodoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaPeriodoNaoEncontradoException(Long periodoLetivoId) {
        super("Período letivo não encontrado para o id " + periodoLetivoId);
    }
}
