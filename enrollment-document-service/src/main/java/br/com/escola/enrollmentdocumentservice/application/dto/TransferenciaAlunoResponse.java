package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferenciaAlunoResponse(
        UUID id,
        UUID alunoId,
        EscolaOrigemResponse escolaOrigem,
        String serieOrigem,
        String anoLetivoOrigem,
        LocalDate dataTransferencia,
        String motivoTransferencia,
        String situacaoOrigem,
        String documentosEntregues,
        String observacao,
        String tipoTransferencia,
        String statusTransferencia,
        String usuarioOperacao,
        LocalDateTime dataHoraOperacao,
        LocalDateTime createdAt) {
}
