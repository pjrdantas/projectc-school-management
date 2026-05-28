package br.com.escola.schoolhistory.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DisciplinaRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,
        Integer cargaHoraria,
        String status) {
}
