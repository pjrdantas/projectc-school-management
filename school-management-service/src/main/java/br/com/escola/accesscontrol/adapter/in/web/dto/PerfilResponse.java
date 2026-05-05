package br.com.escola.accesscontrol.adapter.in.web.dto;

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