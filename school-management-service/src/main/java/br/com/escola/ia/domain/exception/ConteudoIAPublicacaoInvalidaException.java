package br.com.escola.ia.domain.exception;

public class ConteudoIAPublicacaoInvalidaException extends RuntimeException {

    public ConteudoIAPublicacaoInvalidaException() {
        super("Apenas conteúdo aprovado pode ser publicado na biblioteca pedagógica.");
    }
}
