package br.com.escola.enrollment.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollment.adapter.out.persistence.entity.StatusEtapaMatriculaEntity;

public interface StatusEtapaMatriculaJpaRepository extends JpaRepository<StatusEtapaMatriculaEntity, UUID> {

    Optional<StatusEtapaMatriculaEntity> findByCodigoIgnoreCase(String codigo);
}

