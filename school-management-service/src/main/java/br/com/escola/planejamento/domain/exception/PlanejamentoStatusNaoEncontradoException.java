package br.com.escola.planejamento.domain.exception;

public class PlanejamentoStatusNaoEncontradoException extends RuntimeException {

    public PlanejamentoStatusNaoEncontradoException() {
        super("Status de planejamento não encontrado.");
    }
}
