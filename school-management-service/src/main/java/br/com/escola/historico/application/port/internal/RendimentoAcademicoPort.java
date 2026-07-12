package br.com.escola.historico.application.port.internal;

import java.util.UUID;

import br.com.escola.historico.application.dto.internal.RendimentoAcademicoResumo;

public interface RendimentoAcademicoPort {

    RendimentoAcademicoResumo consultarPorMatricula(UUID matriculaId, UUID escolaId);
}
