package br.com.escola.enrollment.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollment.adapter.out.persistence.entity.StatusMatriculaEntity;

public interface StatusMatriculaJpaRepository extends JpaRepository<StatusMatriculaEntity, UUID> {

    Optional<StatusMatriculaEntity> findByCodigoIgnoreCase(String codigo);
}

