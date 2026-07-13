package br.com.escola.pedagogicalservice.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BoletimItemResponse(
        UUID disciplinaId,
        String disciplinaNome,
        BigDecimal media,
        BigDecimal frequenciaPercentual,
        long totalAvaliacoes,
        long totalFrequencias,
        String resultado) {
}
