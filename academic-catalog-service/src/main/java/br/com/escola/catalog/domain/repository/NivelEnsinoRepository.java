package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.NivelEnsino;

public interface NivelEnsinoRepository {

    Optional<NivelEnsino> buscarPorId(UUID id);

    List<NivelEnsino> listarNiveisEnsino();
}
