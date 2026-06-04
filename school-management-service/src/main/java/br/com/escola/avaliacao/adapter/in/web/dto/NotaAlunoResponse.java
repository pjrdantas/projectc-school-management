package br.com.escola.avaliacao.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotaAlunoResponse(
        UUID id,
        UUID avaliacaoId,
        String avaliacaoTitulo,
        UUID matriculaId,
        UUID alunoId,
        String alunoNome,
        BigDecimal nota,
        String observacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
