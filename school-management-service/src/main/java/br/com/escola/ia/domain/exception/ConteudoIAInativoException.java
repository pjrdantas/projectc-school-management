package br.com.escola.ia.domain.exception;

public class ConteudoIAInativoException extends RuntimeException {

    public ConteudoIAInativoException() {
        super("Conteúdo de IA inativo não pode ser versionado, aprovado ou publicado.");
    }
}
