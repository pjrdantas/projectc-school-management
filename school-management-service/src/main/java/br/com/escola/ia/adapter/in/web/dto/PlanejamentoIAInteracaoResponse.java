package br.com.escola.ia.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoIAInteracaoResponse(
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
