package br.com.escola.academiccatalog.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.TurnoEntity;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, UUID> {

    Optional<TurnoEntity> findByCodigoIgnoreCase(String codigo);
}

