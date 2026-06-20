package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.PeriodoLetivo;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface PeriodoLetivoRepository {

    PeriodoLetivo salvar(PeriodoLetivo periodo);

    Optional<PeriodoLetivo> buscarPeriodoPorId(UUID id, EscolaId escolaId);

    List<PeriodoLetivo> listarPeriodos(EscolaId escolaId);
}

