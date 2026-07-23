package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatriculaEtapaResponse(
        UUID id,
        String descricao,
        Integer ordem,
        String status,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusao,
        String observacao) {
}
