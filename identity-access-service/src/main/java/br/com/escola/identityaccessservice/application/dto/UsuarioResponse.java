package br.com.escola.identityaccessservice.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.PerfilVinculado;

public record UsuarioResponse(
        UUID id,
        String username,
        String login,
        String nome,
        String email,
        boolean ativo,
        UUID escolaId,
        LocalDateTime createdAt,
        Set<UUID> perfilIds,
        Set<UUID> perfisIds,
        List<PerfilVinculado> perfis) {
}
