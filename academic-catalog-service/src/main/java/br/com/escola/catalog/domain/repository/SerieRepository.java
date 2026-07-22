package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.Serie;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface SerieRepository {

    Serie salvar(Serie serie);

    void excluirSerie(UUID id, EscolaId escolaId);

    Optional<Serie> buscarSeriePorId(UUID id, EscolaId escolaId);

    List<Serie> listarSeries(EscolaId escolaId);
}

