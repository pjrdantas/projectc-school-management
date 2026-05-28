package br.com.escola.academiccatalog.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.NivelEnsinoEntity;

public interface NivelEnsinoJpaRepository extends JpaRepository<NivelEnsinoEntity, UUID> {

    Optional<NivelEnsinoEntity> findByCodigoIgnoreCase(String codigo);
}

