package br.com.escola.seguranca.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record ContextoAutenticadoResumo(
        UUID usuarioId,
        UUID escolaId,
        String escolaNome,
        String username,
        List<String> perfis,
        List<String> permissoes
) {}
