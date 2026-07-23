package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AtualizarMatriculaCommand(
        UUID turmaId,
        UUID serieId,
        UUID periodoLetivoId,
        String tipoMatricula,
        LocalDate dataMatricula,
        String observacao) {
}
