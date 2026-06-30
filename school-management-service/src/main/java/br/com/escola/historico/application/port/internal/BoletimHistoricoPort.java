package br.com.escola.historico.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.historico.application.dto.internal.BoletimHistoricoResumo;

public interface BoletimHistoricoPort {

    Optional<BoletimHistoricoResumo> buscarParaGeracao(UUID boletimId, UUID escolaId);
}
