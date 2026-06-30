package br.com.escola.historico.application.dto.internal;

import java.math.BigDecimal;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;

public record BoletimHistoricoItemResumo(
        PeriodoLetivoEntity periodoLetivo,
        SerieEntity serieEntity,
        DisciplinaEntity disciplina,
        String componenteCurricular,
        Integer anoLetivo,
        String serie,
        BigDecimal media,
        BigDecimal frequenciaPercentual,
        Integer totalAulas,
        Integer cargaHoraria,
        String resultado) {
}
