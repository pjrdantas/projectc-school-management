package br.com.escola.ia.domain.exception;

public class ConteudoIAVersaoNaoEncontradaException extends RuntimeException {

    public ConteudoIAVersaoNaoEncontradaException() {
        super("Versão de conteúdo de IA não encontrada.");
    }
}
