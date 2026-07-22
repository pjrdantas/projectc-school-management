package br.com.escola.planningaiservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoBimestralAvaliacaoResponse(
        UUID id,
        UUID planejamentoBimestralId,
        String titulo,
        LocalDate dataPrevista,
        BigDecimal peso,
        BigDecimal valorMaximo,
        String tipoAvaliacao,
        LocalDateTime createdAt) {
}
