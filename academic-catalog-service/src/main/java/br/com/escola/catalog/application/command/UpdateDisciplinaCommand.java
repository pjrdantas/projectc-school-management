package br.com.escola.catalog.application.command;

public record UpdateDisciplinaCommand(String nome, Integer cargaHoraria, boolean ativo) {
}
