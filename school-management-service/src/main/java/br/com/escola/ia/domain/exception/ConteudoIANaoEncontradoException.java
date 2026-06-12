package br.com.escola.ia.domain.exception;

public class ConteudoIANaoEncontradoException extends RuntimeException {

    public ConteudoIANaoEncontradoException() {
        super("Conteúdo de IA não encontrado.");
    }
}
