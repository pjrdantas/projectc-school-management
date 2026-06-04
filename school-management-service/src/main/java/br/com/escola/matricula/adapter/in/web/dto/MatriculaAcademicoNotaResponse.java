package br.com.escola.matricula.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MatriculaAcademicoNotaResponse(
        UUID notaId,
        UUID avaliacaoId,
        String avaliacaoTitulo,
        LocalDate dataAplicacao,
        UUID disciplinaId,
        String disciplinaNome,
        String tipoAvaliacao,
        BigDecimal nota,
        BigDecimal valorMaximo,
        BigDecimal peso,
        String observacao) {
}
