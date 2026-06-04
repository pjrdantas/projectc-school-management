package br.com.escola.matricula.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MatriculaAcademicoFrequenciaResponse(
        UUID frequenciaId,
        UUID aulaId,
        LocalDate dataAula,
        UUID disciplinaId,
        String disciplinaNome,
        String situacao,
        String justificativa) {
}
