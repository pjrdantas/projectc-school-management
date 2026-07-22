package br.com.escola.bff.application.dto;

import java.time.LocalDate;

public record PeriodoLetivoUpdateCommand(String nome, Integer ano, LocalDate dataInicio, LocalDate dataFim, boolean ativo) {
}
