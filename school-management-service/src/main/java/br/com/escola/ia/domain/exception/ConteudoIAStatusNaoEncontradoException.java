package br.com.escola.ia.domain.exception;

public class ConteudoIAStatusNaoEncontradoException extends RuntimeException {

    public ConteudoIAStatusNaoEncontradoException() {
        super("Status de conteúdo de IA não encontrado.");
    }
}
