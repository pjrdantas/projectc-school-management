package br.com.escola.bff.application.dto;

public record SerieUpdateCommand(String nome, Integer ordem, String nivelEnsino) {
}
