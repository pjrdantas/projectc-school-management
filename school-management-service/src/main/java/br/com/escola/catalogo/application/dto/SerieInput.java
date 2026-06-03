package br.com.escola.catalogo.application.dto;

public record SerieInput(
        String nome,
        Integer ordem,
        String nivelEnsino) {
}
