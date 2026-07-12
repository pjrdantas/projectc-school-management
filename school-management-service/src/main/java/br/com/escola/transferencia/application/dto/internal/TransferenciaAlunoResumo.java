package br.com.escola.transferencia.application.dto.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferenciaAlunoResumo(
        UUID id,
        UUID alunoId,
        EscolaOrigemResumo escolaOrigem,
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
