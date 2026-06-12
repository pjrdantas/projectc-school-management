package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;

public interface TurmaJpaRepository extends JpaRepository<TurmaEntity, UUID> {

    Optional<TurmaEntity> findByCodigoAndPeriodoLetivo_Id(String codigo, UUID periodoLetivoId);

    Optional<TurmaEntity> findByCodigoAndPeriodoLetivo_IdAndEscola_Id(String codigo, UUID periodoLetivoId, UUID escolaId);

    List<TurmaEntity> findAllByEscola_Id(UUID escolaId);

    Optional<TurmaEntity> findByIdAndEscola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndEscola_Id(UUID id, UUID escolaId);
}
