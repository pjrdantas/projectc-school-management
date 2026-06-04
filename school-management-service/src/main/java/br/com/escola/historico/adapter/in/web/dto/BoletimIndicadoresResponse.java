package br.com.escola.historico.adapter.in.web.dto;

import java.math.BigDecimal;

public record BoletimIndicadoresResponse(
        int totalDisciplinas,
        BigDecimal mediaGeral,
        BigDecimal frequenciaGeralPercentual,
        String resultadoGeral) {
}
