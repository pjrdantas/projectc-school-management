package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.TurmaDisciplinaJpaEntity;

public interface TurmaDisciplinaJpaRepository extends JpaRepository<TurmaDisciplinaJpaEntity, UUID> {

    Optional<TurmaDisciplinaJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    boolean existsByDisciplinaIdAndEscolaId(UUID disciplinaId, UUID escolaId);

    List<TurmaDisciplinaJpaEntity> findAllByTurmaIdAndEscolaId(UUID turmaId, UUID escolaId);
}
