package br.com.escola.planejamento.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoBimestralAulaResponse(
        UUID id,
        UUID planejamentoBimestralId,
        Integer numeroAula,
        String temaAula,
        String objetivoAula,
        String conteudoPrevisto,
        String metodologia,
        String recursos,
        String atividadePrevista,
        String observacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
