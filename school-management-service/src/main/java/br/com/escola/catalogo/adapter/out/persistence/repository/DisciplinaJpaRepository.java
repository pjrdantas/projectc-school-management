package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;

public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaEntity, UUID> {

    List<DisciplinaEntity> findAllByEscola_Id(UUID escolaId);

    Optional<DisciplinaEntity> findByIdAndEscola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndEscola_Id(UUID id, UUID escolaId);
}
