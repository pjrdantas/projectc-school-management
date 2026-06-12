package br.com.escola.institucional.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;

public interface EscolaJpaRepository extends JpaRepository<EscolaEntity, UUID> {

    Optional<EscolaEntity> findFirstByAtivoTrueOrderByCreatedAtAsc();
}
