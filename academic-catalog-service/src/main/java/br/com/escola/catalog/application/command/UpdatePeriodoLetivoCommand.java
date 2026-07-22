package br.com.escola.catalog.application.command;

import java.time.LocalDate;

public record UpdatePeriodoLetivoCommand(
        String nome,
        int ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo) {
}
