package br.com.escola.seguranca.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String username,
        String nome,
        String email,
        boolean ativo,
        LocalDateTime createdAt,
        List<UUID> perfilIds,
        Set<String> perfis,
        Set<String> perfilNomes
) {}
