package br.com.escola.seguranca.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record SessaoAutenticadaResumo(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID usuarioId,
        UUID professorId,
        UUID escolaId,
        String escolaNome,
        String username,
        String nome,
        List<String> perfis,
        List<String> permissoes
) {}
