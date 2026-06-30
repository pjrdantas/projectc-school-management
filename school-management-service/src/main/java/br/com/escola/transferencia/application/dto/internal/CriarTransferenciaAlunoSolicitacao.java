package br.com.escola.transferencia.application.dto.internal;

import java.time.LocalDate;
import java.util.UUID;

public record CriarTransferenciaAlunoSolicitacao(
        UUID alunoId,
        UUID escolaOrigemId,
        EscolaOrigemSolicitacao escolaOrigem,
        String serieOrigem,
        String anoLetivoOrigem,
        LocalDate dataTransferencia,
        String motivoTransferencia,
        String situacaoOrigem,
        String documentosEntregues,
        String tipoTransferencia,
        String statusTransferencia,
        String usuarioOperacao,
        String observacao) {
}
