package br.com.escola.ia.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.ia.application.dto.internal.PlanejamentoIAResumo;

public interface PlanejamentoIAPort {

    Optional<PlanejamentoIAResumo> buscarResumo(UUID planejamentoId, UUID escolaId);

    boolean existePlanejamento(UUID planejamentoId, UUID escolaId);
}
