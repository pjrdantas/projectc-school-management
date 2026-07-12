package br.com.escola.historico.application.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record BoletimHistoricoItemResumo(
        UUID periodoLetivoId,
        UUID serieId,
        UUID disciplinaId,
        String componenteCurricular,
        Integer anoLetivo,
        String serie,
        BigDecimal media,
        BigDecimal frequenciaPercentual,
        Integer totalAulas,
        Integer cargaHoraria,
        String resultado) {
}
