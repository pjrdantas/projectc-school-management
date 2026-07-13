package br.com.escola.planningaiservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoIaInteracaoResponse(
        UUID id,
        UUID planejamentoBimestralId,
        UUID escolaId,
        String escolaNome,
        String promptProfessor,
        String respostaIA,
        String modeloIA,
        Integer tokensEntrada,
        Integer tokensSaida,
        BigDecimal custoEstimado,
        LocalDateTime createdAt) {
}
