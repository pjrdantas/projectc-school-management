package br.com.escola.bff.application.dto;

import java.util.UUID;

public record TurmaUpdateCommand(
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        String turno,
        String status) {
}
