package br.com.escola.bff.application.dto;

import java.util.UUID;

public record DisciplinaCreateCommand(
        String nome,
        Integer cargaHoraria,
        String status,
        UUID escolaId
) {
}
