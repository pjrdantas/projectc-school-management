package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;

public interface TurmaDisciplinaJpaRepository extends JpaRepository<TurmaDisciplinaEntity, UUID> {

    List<TurmaDisciplinaEntity> findByTurmaId(UUID turmaId);

    Optional<TurmaDisciplinaEntity> findByTurmaIdAndDisciplinaId(UUID turmaId, UUID disciplinaId);

    Optional<TurmaDisciplinaEntity> findByIdAndTurma_Escola_Id(UUID id, UUID escolaId);
}
