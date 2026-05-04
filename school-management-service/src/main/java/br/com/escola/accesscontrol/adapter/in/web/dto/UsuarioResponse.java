package br.com.escola.accesscontrol.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String username,
        String nome,
        String email,
        boolean ativo,
        LocalDateTime createdAt
) {}
