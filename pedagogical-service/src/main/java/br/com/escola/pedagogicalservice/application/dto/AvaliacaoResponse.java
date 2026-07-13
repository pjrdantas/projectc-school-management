package br.com.escola.pedagogicalservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AvaliacaoResponse(
        UUID id,
        UUID professorTurmaDisciplinaId,
        UUID professorId,
        String professorNome,
        UUID turmaId,
        String turmaNome,
        UUID escolaId,
        String escolaNome,
        UUID disciplinaId,
        String disciplinaNome,
        String titulo,
        String descricao,
        LocalDate dataAplicacao,
        BigDecimal valorMaximo,
        BigDecimal peso,
        String tipoAvaliacao,
        LocalDateTime createdAt) {
}
