package br.com.escola.bff.application.dto;

public record DisciplinaUpdateCommand(String nome, Integer cargaHoraria, String status) {
}
