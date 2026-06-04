package br.com.escola.matricula.adapter.in.web.dto;

import java.math.BigDecimal;

public record MatriculaAcademicoIndicadoresResponse(
        long totalFrequencias,
        long presencas,
        long faltas,
        long faltasJustificadas,
        long totalNotas,
        BigDecimal mediaNotas,
        BigDecimal mediaPercentual) {
}
