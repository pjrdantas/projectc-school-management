package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaCatalogoResponse(
        UUID id,
        String codigo,
        String descricao) {
}


