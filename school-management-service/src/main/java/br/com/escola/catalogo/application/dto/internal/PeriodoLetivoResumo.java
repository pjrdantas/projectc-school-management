package br.com.escola.catalogo.application.dto.internal;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoLetivoResumo(
        UUID id,
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo
) {
}
