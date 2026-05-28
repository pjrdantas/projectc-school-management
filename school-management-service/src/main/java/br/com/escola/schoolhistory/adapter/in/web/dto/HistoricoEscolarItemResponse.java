package br.com.escola.schoolhistory.adapter.in.web.dto;

import java.util.UUID;

public record HistoricoEscolarItemResponse(
        UUID id,
        String componenteCurricular,
        Integer anoLetivo,
        String serie,
        String ciclo,
        String notaConceito,
        Integer totalAulas,
        Integer cargaHoraria) {
}
