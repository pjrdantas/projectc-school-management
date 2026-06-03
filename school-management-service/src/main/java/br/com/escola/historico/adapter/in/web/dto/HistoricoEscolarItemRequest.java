package br.com.escola.historico.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record HistoricoEscolarItemRequest(
        @NotBlank(message = "componenteCurricular é obrigatório")
        String componenteCurricular,
        @PositiveOrZero(message = "anoLetivo não pode ser negativo")
        Integer anoLetivo,
        String serie,
        String ciclo,
        String notaConceito,
        @PositiveOrZero(message = "totalAulas não pode ser negativo")
        Integer totalAulas,
        @PositiveOrZero(message = "cargaHoraria não pode ser negativa")
        Integer cargaHoraria) {
}
