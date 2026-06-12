package br.com.escola.planejamento.domain.exception;

public class PlanejamentoBimestralNaoEncontradoException extends RuntimeException {

    public PlanejamentoBimestralNaoEncontradoException() {
        super("Planejamento bimestral não encontrado.");
    }
}
