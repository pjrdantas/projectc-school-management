package br.com.escola.catalogo.application.dto;

import java.util.UUID;

public record TurmaInput(
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        String turno,
        String status) {
}
