package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;

public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoEntity, UUID> {

    List<PeriodoLetivoEntity> findAllByEscola_Id(UUID escolaId);

    Optional<PeriodoLetivoEntity> findByIdAndEscola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndEscola_Id(UUID id, UUID escolaId);
}
