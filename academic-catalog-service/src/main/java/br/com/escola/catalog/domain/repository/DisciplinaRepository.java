package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalog.domain.model.Disciplina;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface DisciplinaRepository {

    Disciplina salvar(Disciplina disciplina);

    void excluirDisciplina(UUID id, EscolaId escolaId);

    Optional<Disciplina> buscarDisciplinaPorId(UUID id, EscolaId escolaId);

    List<Disciplina> listarDisciplinas(EscolaId escolaId);
}

