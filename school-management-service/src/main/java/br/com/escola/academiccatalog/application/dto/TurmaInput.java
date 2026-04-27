package br.com.escola.academiccatalog.application.dto;

public record TurmaInput(
        String codigo,
        String nome,
        Integer capacidade,
        Long periodoLetivoId) {
}
