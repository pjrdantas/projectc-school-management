package br.com.escola.historico.application.dto.internal;

import java.util.List;

public record RendimentoAcademicoResumo(
        List<FrequenciaAcademicaResumo> frequencias,
        List<NotaAcademicaResumo> notas) {
}
