package br.com.escola.seguranca.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record PerfilResponse(
        UUID id,
        String codigo,
        String nome,
        String descricao,
        LocalDateTime createdAt,
        Set<PermissaoResponse> permissoes
) {}