package br.com.escola.identityaccessservice.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;

public record PerfilResponse(
        UUID id,
        String codigo,
        String nome,
        String nmPerfil,
        String descricao,
        LocalDateTime createdAt,
        Set<UUID> permissaoIds,
        Set<UUID> permissoesIds,
        List<PermissaoAdministrada> permissoes) {
}
