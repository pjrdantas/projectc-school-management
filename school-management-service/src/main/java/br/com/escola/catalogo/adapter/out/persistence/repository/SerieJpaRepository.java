package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;

public interface SerieJpaRepository extends JpaRepository<SerieEntity, UUID> {

    List<SerieEntity> findAllByEscola_Id(UUID escolaId);

    Optional<SerieEntity> findByIdAndEscola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndEscola_Id(UUID id, UUID escolaId);
}
