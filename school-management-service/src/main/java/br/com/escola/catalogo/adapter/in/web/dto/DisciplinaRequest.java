package br.com.escola.catalogo.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DisciplinaRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,
        Integer cargaHoraria,
        String status) {
}
