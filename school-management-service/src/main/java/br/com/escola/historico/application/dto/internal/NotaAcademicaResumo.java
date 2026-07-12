package br.com.escola.historico.application.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NotaAcademicaResumo(
        UUID id,
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
