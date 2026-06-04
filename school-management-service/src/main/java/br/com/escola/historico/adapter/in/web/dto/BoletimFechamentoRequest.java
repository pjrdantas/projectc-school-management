package br.com.escola.historico.adapter.in.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoletimFechamentoRequest(
        @NotBlank
        @Size(max = 60)
        String periodoReferencia,
        LocalDate dataFechamento,
        @Size(max = 4000)
        String observacao,
        Boolean sobrescrever) {
}
