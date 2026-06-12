package br.com.escola.ia.domain.exception;

public class ConteudoIATipoNaoEncontradoException extends RuntimeException {

    public ConteudoIATipoNaoEncontradoException() {
        super("Tipo de conteúdo de IA não encontrado.");
    }
}
