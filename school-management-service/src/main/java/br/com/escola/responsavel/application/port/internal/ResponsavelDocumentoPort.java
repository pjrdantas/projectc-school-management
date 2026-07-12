package br.com.escola.responsavel.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.responsavel.adapter.out.persistence.entity.ResponsavelEntity;

public interface ResponsavelDocumentoPort {

    Optional<ResponsavelEntity> buscarResponsavelPorIdEEscola(UUID responsavelId, UUID escolaId);
}
