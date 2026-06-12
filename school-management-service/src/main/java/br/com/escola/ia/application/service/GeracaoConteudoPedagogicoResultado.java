package br.com.escola.ia.application.service;

public record GeracaoConteudoPedagogicoResultado(
        String modelo,
        String conteudo,
        int tokensEntrada,
        int tokensSaida) {
}
