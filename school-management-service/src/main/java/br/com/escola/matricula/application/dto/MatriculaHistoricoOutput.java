package br.com.escola.matricula.application.dto;

import java.util.UUID;

public record MatriculaHistoricoOutput(
        UUID matriculaId,
        UUID turmaId,
        UUID periodoLetivoId,
        Integer serieOrdem,
        String status) {
}
