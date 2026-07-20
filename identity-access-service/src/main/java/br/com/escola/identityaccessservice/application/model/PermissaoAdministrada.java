package br.com.escola.identityaccessservice.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissaoAdministrada(
        UUID id,
        String codigo,
        String nmPermissao,
        String descricao,
        LocalDateTime createdAt) {
}
