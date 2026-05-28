package br.com.escola.enrollment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatriculaEtapaOutput(
        UUID id,
        String descricao,
        Integer ordem,
        String status,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusao,
        String observacao) {
}

