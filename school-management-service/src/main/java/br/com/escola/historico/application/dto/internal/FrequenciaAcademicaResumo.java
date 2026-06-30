package br.com.escola.historico.application.dto.internal;

import java.time.LocalDate;
import java.util.UUID;

public record FrequenciaAcademicaResumo(
        UUID id,
        UUID aulaId,
        LocalDate dataAula,
        UUID disciplinaId,
        String disciplinaNome,
        String situacao,
        String justificativa) {
}
