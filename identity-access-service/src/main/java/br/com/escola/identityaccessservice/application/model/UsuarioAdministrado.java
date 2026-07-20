package br.com.escola.identityaccessservice.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UsuarioAdministrado(
        UUID id,
        String username,
        String nome,
        String email,
        boolean ativo,
        UUID escolaId,
        LocalDateTime createdAt,
        List<PerfilVinculado> perfis) {
}
