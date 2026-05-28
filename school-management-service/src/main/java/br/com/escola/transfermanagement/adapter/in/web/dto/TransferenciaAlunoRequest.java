package br.com.escola.transfermanagement.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferenciaAlunoRequest(
        @NotNull(message = "alunoId é obrigatório")
        UUID alunoId,
        UUID escolaOrigemId,
        @Valid
        EscolaOrigemRequest escolaOrigem,
        @NotBlank(message = "serieOrigem é obrigatória")
        String serieOrigem,
        @NotBlank(message = "anoLetivoOrigem é obrigatório")
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
