package br.com.escola.historico.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoricoEscolarGeracaoRequest(
        @NotNull
        UUID boletimId,
        Boolean sobrescrever,
        @Size(max = 120)
        String ensinoConcluido,
        @Size(max = 4000)
        String observacoes) {
}
