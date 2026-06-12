package br.com.escola.planejamento.domain.exception;

public class PlanejamentoPeriodoAvaliativoNaoEncontradoException extends RuntimeException {

    public PlanejamentoPeriodoAvaliativoNaoEncontradoException() {
        super("Período avaliativo não encontrado.");
    }
}
