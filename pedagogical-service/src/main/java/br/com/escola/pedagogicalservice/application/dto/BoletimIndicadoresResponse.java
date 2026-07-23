package br.com.escola.pedagogicalservice.application.dto;

import java.math.BigDecimal;

public record BoletimIndicadoresResponse(
        int totalDisciplinas,
        BigDecimal mediaGeral,
        BigDecimal frequenciaGeralPercentual,
        String resultadoGeral) {
}
