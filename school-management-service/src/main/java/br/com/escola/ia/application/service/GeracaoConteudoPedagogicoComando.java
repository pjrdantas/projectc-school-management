package br.com.escola.ia.application.service;

public record GeracaoConteudoPedagogicoComando(
        String tituloPlanejamento,
        String temaPrincipal,
        String descricaoInicial,
        String objetivoGeral,
        String turmaNome,
        String disciplinaNome,
        String tipoConteudo,
        String promptProfessor) {
}
