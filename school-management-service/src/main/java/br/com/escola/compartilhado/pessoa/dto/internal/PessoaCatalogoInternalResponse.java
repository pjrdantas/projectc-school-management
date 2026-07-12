package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.UUID;

public record PessoaCatalogoInternalResponse(
        UUID id,
        String codigo,
        String descricao) {
}
