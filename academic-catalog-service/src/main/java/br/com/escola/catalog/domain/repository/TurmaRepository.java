package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.Turma;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface TurmaRepository {

    Turma salvar(Turma turma);

    Optional<Turma> buscarTurmaPorId(UUID id, EscolaId escolaId);

    List<Turma> listarTurmas(EscolaId escolaId);
}

