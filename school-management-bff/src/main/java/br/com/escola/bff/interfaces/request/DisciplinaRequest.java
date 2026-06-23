package br.com.escola.bff.interfaces.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record DisciplinaRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,
        Integer cargaHoraria,
        String status,
        UUID escolaId
) {
}
