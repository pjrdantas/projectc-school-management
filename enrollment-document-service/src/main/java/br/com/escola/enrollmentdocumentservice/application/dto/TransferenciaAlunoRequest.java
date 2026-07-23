package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferenciaAlunoRequest(
        @NotNull(message = "alunoId e obrigatorio")
        UUID alunoId,
        UUID escolaOrigemId,
        @Valid
        EscolaOrigemRequest escolaOrigem,
        @NotBlank(message = "serieOrigem e obrigatoria")
        String serieOrigem,
        @NotBlank(message = "anoLetivoOrigem e obrigatorio")
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
