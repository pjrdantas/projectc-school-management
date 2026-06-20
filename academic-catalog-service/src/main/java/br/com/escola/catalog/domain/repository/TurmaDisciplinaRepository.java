package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.TurmaDisciplina;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface TurmaDisciplinaRepository {

    TurmaDisciplina salvar(TurmaDisciplina vinculo);

    Optional<TurmaDisciplina> buscarVinculoPorId(UUID id, EscolaId escolaId);

    List<TurmaDisciplina> listarVinculosPorTurma(UUID turmaId, EscolaId escolaId);
}
