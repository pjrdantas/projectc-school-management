package br.com.escola.institucional.application.port.internal;

import java.util.List;
import java.util.UUID;

public interface UsuarioEscolaPort {

    void garantirVinculo(UUID usuarioId, UUID escolaId);

    List<UUID> listarEscolasDoUsuario(UUID usuarioId);
}
