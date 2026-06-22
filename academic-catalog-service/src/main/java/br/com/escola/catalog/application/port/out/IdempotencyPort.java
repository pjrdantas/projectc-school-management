package br.com.escola.catalog.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.application.idempotency.CommandIdempotency;

public interface IdempotencyPort {

    void bloquear(UUID escolaId, String key);

    Optional<CommandIdempotency> buscar(UUID escolaId, String key);

    void salvar(CommandIdempotency idempotency);
}
