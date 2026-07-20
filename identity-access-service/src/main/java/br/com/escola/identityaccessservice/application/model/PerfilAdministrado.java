package br.com.escola.identityaccessservice.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PerfilAdministrado(
        UUID id,
        String codigo,
        String nome,
        String descricao,
        LocalDateTime createdAt,
        List<PermissaoAdministrada> permissoes) {
}
