package br.com.escola.planejamento.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoBimestralAvaliacaoResponse(
        UUID id,
        UUID planejamentoBimestralId,
        String titulo,
        String descricao,
        LocalDate dataPrevista,
        BigDecimal peso,
        BigDecimal valorMaximo,
        String tipoAvaliacao,
        String conteudoCobrado,
        String orientacaoAplicacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
