package br.com.escola.seguranca.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record PrincipalAutenticadoResumo(
        UUID usuarioId,
        String username,
        List<String> permissoes
) {
}
