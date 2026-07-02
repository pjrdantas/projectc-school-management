package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.UUID;

public record PessoaCatalogoResumo(
        UUID id,
        String codigo,
        String descricao) {
}
