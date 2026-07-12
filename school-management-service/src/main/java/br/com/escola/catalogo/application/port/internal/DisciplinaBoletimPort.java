package br.com.escola.catalogo.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalogo.application.dto.internal.DisciplinaBoletimResumo;

public interface DisciplinaBoletimPort {

    Optional<DisciplinaBoletimResumo> buscarResumoPorIdEEscola(UUID disciplinaId, UUID escolaId);
}
